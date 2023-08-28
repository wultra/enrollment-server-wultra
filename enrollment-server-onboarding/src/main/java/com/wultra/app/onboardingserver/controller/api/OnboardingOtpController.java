/*
 * PowerAuth integration libraries for RESTful API applications, examples and
 * related software components
 *
 * Copyright (C) 2018 Wultra s.r.o.
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
package com.wultra.app.onboardingserver.controller.api;

import com.wultra.app.enrollmentserver.model.enumeration.OtpStatus;
import com.wultra.app.enrollmentserver.model.enumeration.OtpType;
import com.wultra.app.onboardingserver.common.database.OnboardingOtpRepository;
import com.wultra.app.onboardingserver.common.database.entity.OnboardingOtpEntity;
import com.wultra.app.onboardingserver.common.errorhandling.OnboardingProcessException;
import com.wultra.app.onboardingserver.mock.model.request.OtpDetailRequest;
import com.wultra.app.onboardingserver.mock.model.response.OtpDetailResponse;
import io.getlime.core.rest.model.base.request.ObjectRequest;
import io.getlime.core.rest.model.base.response.ObjectResponse;
import io.getlime.security.powerauth.rest.api.spring.annotation.EncryptedRequestBody;
import io.getlime.security.powerauth.rest.api.spring.annotation.PowerAuthEncryption;
import io.getlime.security.powerauth.rest.api.spring.encryption.EncryptionContext;
import io.getlime.security.powerauth.rest.api.spring.encryption.EncryptionScope;
import io.getlime.security.powerauth.rest.api.spring.exception.PowerAuthEncryptionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * Controller which provides onboarding OTP for tests.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@RestController
@RequestMapping("api/onboarding")
public class OnboardingOtpController {

    private final static Logger logger = LoggerFactory.getLogger(OnboardingOtpController.class);

    private final OnboardingOtpRepository onboardingOtpRepository;

    @Autowired
    public OnboardingOtpController(OnboardingOtpRepository onboardingOtpRepository) {
        this.onboardingOtpRepository = onboardingOtpRepository;
    }

    /**
     * Get onboarding OTP detail for tests.
     *
     * @param request OTP detail request.
     * @param encryptionContext Encryption context.
     * @return OTP detail response.
     * @throws PowerAuthEncryptionException In case encryption or decryption fails.
     */
    @RequestMapping(value = "otp/detail", method = RequestMethod.POST)
    @PowerAuthEncryption(scope = EncryptionScope.APPLICATION_SCOPE)
    public ObjectResponse<OtpDetailResponse> getOtpDetail(@EncryptedRequestBody ObjectRequest<OtpDetailRequest> request,
                                                         EncryptionContext encryptionContext) throws PowerAuthEncryptionException, OnboardingProcessException {

        if (encryptionContext == null) {
            logger.error("Encryption failed");
            throw new PowerAuthEncryptionException();
        }

        if (request.getRequestObject() == null) {
            logger.warn("Missing request object in request");
            throw new OnboardingProcessException();
        }

        String processId = request.getRequestObject().getProcessId();
        if (processId == null) {
            logger.warn("Missing process ID in request");
            throw new OnboardingProcessException();
        }
        OtpType otpType = request.getRequestObject().getOtpType();
        if (otpType == null) {
            logger.warn("Missing OTP type in request");
            throw new OnboardingProcessException();
        }

        Optional<OnboardingOtpEntity> otpOptional = onboardingOtpRepository.findNewestByProcessIdAndType(processId, otpType);
        if (!otpOptional.isPresent()) {
            logger.warn("OTP not found for process ID: {}", processId);
            throw new OnboardingProcessException();
        }

        OnboardingOtpEntity otp = otpOptional.get();
        if (otp.getStatus() != OtpStatus.ACTIVE) {
            logger.warn("OTP is not ACTIVE for process ID: {}", processId);
            throw new OnboardingProcessException();
        }

        OtpDetailResponse otpDetailResponse = new OtpDetailResponse();
        otpDetailResponse.setProcessId(processId);
        otpDetailResponse.setOtpCode(otp.getOtpCode());
        return new ObjectResponse<>(otpDetailResponse);
    }

}