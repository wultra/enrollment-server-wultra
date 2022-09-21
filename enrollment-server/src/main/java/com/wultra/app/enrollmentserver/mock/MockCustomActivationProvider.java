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
package com.wultra.app.enrollmentserver.mock;

import com.wultra.app.enrollmentserver.api.model.onboarding.response.OtpVerifyResponse;
import com.wultra.app.enrollmentserver.model.enumeration.OnboardingStatus;
import com.wultra.app.enrollmentserver.model.integration.OwnerId;
import com.wultra.app.onboardingserver.common.activation.ActivationOtpService;
import com.wultra.app.onboardingserver.common.activation.ActivationProcessService;
import com.wultra.app.onboardingserver.common.errorhandling.OnboardingProcessException;
import com.wultra.app.onboardingserver.common.errorhandling.PowerAuthActivationOtpException;
import com.wultra.app.onboardingserver.common.errorhandling.PowerAuthActivationOtpFailedException;
import io.getlime.security.powerauth.rest.api.model.entity.ActivationType;
import io.getlime.security.powerauth.rest.api.spring.exception.PowerAuthActivationException;
import io.getlime.security.powerauth.rest.api.spring.provider.CustomActivationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.getlime.security.powerauth.rest.api.model.entity.ActivationType.CUSTOM;
import static io.getlime.security.powerauth.rest.api.model.entity.ActivationType.RECOVERY;

/**
 * Default implementation of CustomActivationProvider interface.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Component
public class MockCustomActivationProvider implements CustomActivationProvider {

    private static final Logger logger = LoggerFactory.getLogger(MockCustomActivationProvider.class);

    private final ActivationProcessService onboardingProcessService;
    private final ActivationOtpService onboardingOtpService;

    @Autowired
    public MockCustomActivationProvider(ActivationProcessService onboardingProcessService, ActivationOtpService onboardingOtpService) {
        this.onboardingProcessService = onboardingProcessService;
        this.onboardingOtpService = onboardingOtpService;
    }

    @Override
    public String lookupUserIdForAttributes(Map<String, String> identityAttributes, Map<String, Object> context) throws PowerAuthActivationException {
        // Testing of onboarding process, identityAttributes contain processId and otpCode
        if (identityAttributes.containsKey("processId")) {
            String processId = identityAttributes.get("processId");
            if (processId == null) {
                logger.warn("Missing process ID during custom activation");
                throw new PowerAuthActivationException();
            }
            String otpCode = identityAttributes.get("otpCode");
            if (otpCode == null) {
                logger.warn("Missing OTP code during custom activation for process ID: {}", processId);
                throw new PowerAuthActivationException();
            }
            try {
                // Check onboarding process status
                OnboardingStatus status = onboardingProcessService.getProcessStatus(processId);
                if (status != OnboardingStatus.ACTIVATION_IN_PROGRESS) {
                    logger.warn("Onboarding process is in invalid state for process ID: {}, status: {}", processId, status);
                    throw new PowerAuthActivationException();
                }

                // Lookup user ID using process stored in database
                OtpVerifyResponse verifyResponse = onboardingOtpService.verifyOtpCode(processId, new OwnerId(), otpCode);
                if (verifyResponse.isVerified()) {
                    return onboardingProcessService.getUserId(processId);
                }
                if (verifyResponse.getOnboardingStatus() != OnboardingStatus.ACTIVATION_IN_PROGRESS) {
                    // Onboarding process status is not ACTIVATION_IN_PROGRESS, onboarding process failed
                    logger.info("Onboarding process failed during activation, process ID: {}, status: {}", processId, verifyResponse.getOnboardingStatus());
                    throw new PowerAuthActivationException();
                }
                if (verifyResponse.getRemainingAttempts() > 0) {
                    // There are remaining attempts for OTP verification
                    logger.info("OTP verification attempt failed during activation, process ID: {}, remaining attempts: {}", processId, verifyResponse.getRemainingAttempts());
                    throw new PowerAuthActivationOtpException(verifyResponse.getRemainingAttempts());
                }
                // All attempts for OTP verification have been used, onboarding process failed
                logger.warn("Maximum number of OTP verification attempts reached during activation, process ID: {}, remaining attempts: {}", processId, verifyResponse.getRemainingAttempts());
                throw new PowerAuthActivationOtpFailedException();
            } catch (OnboardingProcessException e) {
                logger.warn("Onboarding process failed, process ID: {}", processId);
                throw new PowerAuthActivationException();
            }
        }
        return identityAttributes.get("username");
    }

    @Override
    public Map<String, Object> processCustomActivationAttributes(Map<String, Object> customAttributes, String activationId, String userId, String appId, ActivationType activationType, Map<String, Object> context) {
        if (customAttributes != null) {
            // Copy custom attributes
            return new HashMap<>(customAttributes);
        } else {
            return Collections.emptyMap();
        }
    }

    @Override
    public boolean shouldAutoCommitActivation(Map<String, String> identityAttributes, Map<String, Object> customAttributes, String activationId, String userId, String appId, ActivationType activationType, Map<String, Object> context) {
        logger.info("Deciding autocommit for activationId={}, activationType={}, identityAttributes={}", activationId, activationType, identityAttributes);
        if (RECOVERY.equals(activationType) || CUSTOM.equals(activationType)) {
            logger.debug("Activation types RECOVERY and CUSTOM are auto-committed for tests");
            return true;
        }
        return false;
    }

    @Override
    public void activationWasCommitted(Map<String, String> identityAttributes, Map<String, Object> customAttributes, String activationId, String userId, String appId, ActivationType activationType, Map<String, Object> context) throws PowerAuthActivationException {
        // Testing of onboarding process, identityAttributes contain processId and otpCode
        if (identityAttributes.containsKey("processId")) {
            String processId = identityAttributes.get("processId");
            try {
                // Update onboarding process
                onboardingProcessService.updateProcess(processId, userId, activationId, OnboardingStatus.VERIFICATION_IN_PROGRESS);
                logger.info("Activation was created for user ID: {}, activation ID: {}, process ID: {}", userId, activationId, processId);
            } catch (OnboardingProcessException e) {
                logger.warn("Onboarding process failed, process ID: {}", processId);
                throw new PowerAuthActivationException();
            }
        }
    }

    @Override
    public Integer getMaxFailedAttemptCount(Map<String, String> identityAttributes, Map<String, Object> customAttributes, String userId, ActivationType activationType, Map<String, Object> context) {
        // Null value means use value configured on PowerAuth server
        return null;
    }

    @Override
    public Long getValidityPeriodDuringActivation(Map<String, String> identityAttributes, Map<String, Object> customAttributes, String userId, ActivationType activationType, Map<String, Object> context) {
        // Null value means use value configured on PowerAuth server
        return null;
    }

    @Override
    public List<String> getActivationFlags(Map<String, String> identityAttributes, Map<String, Object> customAttributes, String activationId, String userId, String appId, ActivationType activationType, Map<String, Object> context) {
        // Testing of onboarding process, the VERIFICATION_PENDING flag needs to be added
        if (identityAttributes.containsKey("processId")) {
            String processId = identityAttributes.get("processId");
            try {
                // Check user ID in onboarding process
                if (!onboardingProcessService.getUserId(processId).equals(userId)) {
                    logger.warn("User ID mismatch for process ID: {}, {}", processId, userId);
                    return Collections.emptyList();
                }
                return Collections.singletonList("VERIFICATION_PENDING");
            } catch (OnboardingProcessException e) {
                logger.warn("Onboarding process failed, process ID: {}", processId);
                return Collections.emptyList();
            }
        }
        return Collections.emptyList();
    }
}
