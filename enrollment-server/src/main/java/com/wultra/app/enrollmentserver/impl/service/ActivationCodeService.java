/*
 * PowerAuth Enrollment Server
 * Copyright (C) 2021 Wultra s.r.o.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.wultra.app.enrollmentserver.impl.service;

import com.wultra.app.enrollmentserver.errorhandling.ActivationCodeException;
import com.wultra.app.enrollmentserver.errorhandling.InvalidRequestObjectException;
import com.wultra.app.enrollmentserver.impl.service.converter.ActivationCodeConverter;
import com.wultra.app.enrollmentserver.model.request.ActivationCodeRequest;
import com.wultra.app.enrollmentserver.model.response.ActivationCodeResponse;
import com.wultra.app.enrollmentserver.model.validator.ActivationCodeRequestValidator;
import com.wultra.security.powerauth.client.PowerAuthClient;
import com.wultra.security.powerauth.client.model.error.PowerAuthClientException;
import com.wultra.security.powerauth.client.v3.ActivationOtpValidation;
import com.wultra.security.powerauth.client.v3.InitActivationResponse;
import io.getlime.security.powerauth.rest.api.base.authentication.PowerAuthApiAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service responsible for fetching the new activation codes.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@Service
public class ActivationCodeService {

    private static final Logger logger = LoggerFactory.getLogger(ActivationCodeService.class);

    private final PowerAuthClient powerAuthClient;
    private final ActivationCodeConverter activationCodeConverter;

    private DelegatingActivationCodeHandler delegatingActivationCodeHandler;

    /**
     * Autowiring constructor.
     *
     * @param powerAuthClient PowerAuth Client instance.
     * @param activationCodeConverter Activation code converter class.
     */
    @Autowired
    public ActivationCodeService(PowerAuthClient powerAuthClient, ActivationCodeConverter activationCodeConverter) {
        this.powerAuthClient = powerAuthClient;
        this.activationCodeConverter = activationCodeConverter;
    }

    /**
     * Set delegating activation code handler via auto-wiring.
     *
     * @param delegatingActivationCodeHandler Delegating activation code handler bean.
     */
    @Autowired(required = false)
    public void setActivationCodeDelegate(DelegatingActivationCodeHandler delegatingActivationCodeHandler) {
        this.delegatingActivationCodeHandler = delegatingActivationCodeHandler;
    }

    /**
     * Request activation code for provided OTP value, user ID and app ID.
     *
     * @param request Request with OTP value.
     * @param apiAuthentication Authentication object.
     * @return Response with activation code, activation code signature, and activation ID.
     * @throws ActivationCodeException In case of invalid user / app attributes or communication with PowerAuth Service fails.
     * @throws InvalidRequestObjectException In case request object validation fails.
     */
    public ActivationCodeResponse requestActivationCode(ActivationCodeRequest request, PowerAuthApiAuthentication apiAuthentication) throws InvalidRequestObjectException, ActivationCodeException {

        // Fetch information from the authentication object
        final String sourceActivationId = apiAuthentication.getActivationId();
        final String sourceUserId = apiAuthentication.getUserId();
        final Long sourceAppId = apiAuthentication.getApplicationId();
        final List<String> sourceActivationFlags = apiAuthentication.getActivationFlags();
        final List<String> sourceApplicationRoles = apiAuthentication.getApplicationRoles();

        logger.info("Activation code registration started, user ID: {}", sourceUserId);

        // Verify that delegating activation code handler is implemented
        if (delegatingActivationCodeHandler == null) {
            logger.error("Missing delegating activation code handler implementation");
            throw new ActivationCodeException();
        }

        // Validate the request object
        final String error = ActivationCodeRequestValidator.validate(request);
        if (error != null) {
            logger.error("Invalid object in activation code request - {}, user ID: {}", error, sourceUserId);
            throw new InvalidRequestObjectException();
        }

        // Get the request parameters
        final String otp = request.getOtp();
        final String applicationId = request.getApplicationId();

        final Long destinationAppId = delegatingActivationCodeHandler.fetchDestinationApplicationId(applicationId, sourceAppId, sourceActivationFlags, sourceApplicationRoles);
        if (destinationAppId == null) {
            logger.error("Invalid application ID. The provided source app ID: {} cannot activate the destination app ID: {}.", sourceAppId, applicationId);
            throw new ActivationCodeException();
        }

        try {
            // Create a new activation
            logger.info("Calling PowerAuth Server with new activation request, user ID: {}, app ID: {}, otp: {}, ", sourceUserId, destinationAppId, otp);
            final InitActivationResponse iar = powerAuthClient.initActivation(
                    sourceUserId, destinationAppId, ActivationOtpValidation.ON_KEY_EXCHANGE, otp
            );
            logger.info("Successfully obtained a new activation with ID: {}", iar.getActivationId());

            // Notify systems about newly created activation
            delegatingActivationCodeHandler.didReturnActivationCode(
                    sourceActivationId, sourceUserId, sourceAppId, destinationAppId,
                    iar.getActivationId(), iar.getActivationCode(), iar.getActivationSignature()
            );

            // Add the activation flags
            final List<String> flags = delegatingActivationCodeHandler.addActivationFlags(
                    sourceActivationId, sourceActivationFlags, sourceUserId, sourceAppId, sourceApplicationRoles, destinationAppId,
                    iar.getActivationId(), iar.getActivationCode(), iar.getActivationSignature()
            );
            if (flags != null && !flags.isEmpty()) {
                logger.info("Calling PowerAuth Server to add activation flags to activation ID: {}, flags: {}.", iar.getActivationId(), flags.toArray());
                powerAuthClient.addActivationFlags(iar.getActivationId(), flags);
                logger.info("Successfully added flags to activation ID: {}.", iar.getActivationId());
            } else {
                logger.info("Activation with ID: {} has no additional flags.", iar.getActivationId());
            }

            return activationCodeConverter.convert(iar);
        } catch (PowerAuthClientException e) {
            logger.error("Unable to call upstream service.", e);
            throw new ActivationCodeException();
        }

    }

}
