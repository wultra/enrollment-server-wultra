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

import com.wultra.app.enrollmentserver.database.entity.OnboardingProcess;
import com.wultra.app.enrollmentserver.errorhandling.OnboardingProcessException;
import com.wultra.app.enrollmentserver.impl.service.OnboardingService;
import com.wultra.app.enrollmentserver.model.enumeration.OnboardingStatus;
import com.wultra.app.enrollmentserver.model.response.OtpVerifyResponse;
import io.getlime.security.powerauth.rest.api.model.entity.ActivationType;
import io.getlime.security.powerauth.rest.api.spring.exception.PowerAuthActivationException;
import io.getlime.security.powerauth.rest.api.spring.provider.CustomActivationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

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

    // TODO - prepare a separate service which should be used within activation to separate internal code
    private final OnboardingService onboardingService;

    @Autowired
    public MockCustomActivationProvider(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    @Override
    public String lookupUserIdForAttributes(Map<String, String> identityAttributes, Map<String, Object> context) throws PowerAuthActivationException {
        // Testing of onboarding process, identityAttributes contain processId and otpCode
        if (identityAttributes.containsKey("processId")) {
            String processId = identityAttributes.get("processId");
            String otpCode = identityAttributes.get("otpCode");
            if (otpCode == null) {
                logger.warn("Missing OTP code during custom activation for process ID: " + processId);
                throw new PowerAuthActivationException();
            }
            try {
                // Lookup user ID using process stored in database
                // TODO - use converter, do not manipulate the entity directly
                OnboardingProcess process = onboardingService.findProcess(processId);
                String userId = process.getUserId();
                // Verify OTP code
                OtpVerifyResponse response = onboardingService.verifyOtp(processId, otpCode);
                if (response.isVerified()) {
                    return userId;
                }
                // TODO - provide a better error message by subclassing PowerAuthActivationException, include remaining attempts
                throw new PowerAuthActivationException();
            } catch (OnboardingProcessException e) {
                logger.warn("Onboarding process failed, process ID: {}", processId);
                throw new PowerAuthActivationException();
            }
        }
        return identityAttributes.get("username");
    }

    @Override
    public Map<String, Object> processCustomActivationAttributes(Map<String, Object> customAttributes, String activationId, String userId, Long applId, ActivationType activationType, Map<String, Object> context) {
        if (customAttributes != null) {
            // Copy custom attributes
            return new HashMap<>(customAttributes);
        } else {
            return Collections.emptyMap();
        }
    }

    @Override
    public boolean shouldAutoCommitActivation(Map<String, String> identityAttributes, Map<String, Object> customAttributes, String activationId, String userId, Long applId, ActivationType activationType, Map<String, Object> context) {
        // Activation types RECOVERY and CUSTOM are auto-committed for tests
        if (RECOVERY.equals(activationType) || CUSTOM.equals(activationType)) {
            return true;
        }
        return false;
    }

    @Override
    public void activationWasCommitted(Map<String, String> identityAttributes, Map<String, Object> customAttributes, String activationId, String userId, Long applId, ActivationType activationType, Map<String, Object> context) throws PowerAuthActivationException {
        // Testing of onboarding process, identityAttributes contain processId and otpCode
        if (identityAttributes.containsKey("processId")) {
            String processId = identityAttributes.get("processId");
            try {
                // Lookup user ID using process stored in database
                OnboardingProcess process = onboardingService.findProcess(processId);
                if (!userId.equals(process.getUserId())) {
                    logger.warn("Invalid user ID: {}", userId);
                    throw new PowerAuthActivationException();
                }
                process.setStatus(OnboardingStatus.FINISHED);
                process.setActivationId(activationId);
                process.setTimestampLastUpdated(new Date());
                // TODO - use converter, do not manipulate the entity directly
                onboardingService.updateProcess(process);
                logger.info("Onboarding succeeded for user ID: {}, activation ID: {}, process ID: {}", userId, activationId, processId);
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

    public List<String> getActivationFlags(Map<String, String> identityAttributes, Map<String, Object> customAttributes, String activationId, String userId, Long appId, ActivationType activationType, Map<String, Object> context) {
        // Testing of onboarding process, the VERIFICATION_PENDING flag needs to be added
        if (identityAttributes.containsKey("processId")) {
            String processId = identityAttributes.get("processId");
            try {
                // Lookup user ID using process stored in database
                OnboardingProcess process = onboardingService.findProcess(processId);
                if (!userId.equals(process.getUserId())) {
                    logger.warn("Invalid user ID: {}", userId);
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
