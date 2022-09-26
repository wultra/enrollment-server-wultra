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
package com.wultra.app.onboardingserver.mock;

import com.wultra.app.onboardingserver.errorhandling.OnboardingProviderException;
import com.wultra.app.onboardingserver.provider.OnboardingProvider;
import com.wultra.app.onboardingserver.provider.model.request.*;
import com.wultra.app.onboardingserver.provider.model.response.ApproveConsentResponse;
import com.wultra.app.onboardingserver.provider.model.response.EvaluateClientResponse;
import com.wultra.app.onboardingserver.provider.model.response.LookupUserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Locale;
import java.util.Map;

/**
 * Mock onboarding provider for testing purposes.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Component
public class MockOnboardingProvider implements OnboardingProvider {

    private static final Logger logger = LoggerFactory.getLogger(MockOnboardingProvider.class);

    @Value("${enrollment-server-onboarding.presence-check.mock.consent-text}")
    private String consentText;

    @Override
    public LookupUserResponse lookupUser(LookupUserRequest request) throws OnboardingProviderException {
        final Map<String, Object> identification = request.getIdentification();
        logger.info("Lookup user called: {}", identification);

        if (Boolean.TRUE.equals(identification.get("shouldFail"))) {
            logger.info("Mock asked to fail via identification flag");
            throw new OnboardingProviderException("Mock asked to fail");
        }

        return LookupUserResponse.builder()
                .userId("mockuser_" + identification.get("clientNumber"))
                .consentRequired(true)
                .build();
    }

    @Override
    public void sendOtpCode(SendOtpCodeRequest request) {
        final String userId = request.getUserId();
        final String otpCode = request.getOtpCode();
        final boolean resend = request.isResend();
        logger.info("Send OTP code called, user ID: {}, OTP code: {}, resend: {}", userId, otpCode, resend);
    }

    @Override
    public String fetchConsent(final ConsentTextRequest request) {
        final String processId = request.getProcessId();
        final String userId = request.getUserId();
        final String consentType = request.getConsentType();
        final Locale locale = request.getLocale();
        logger.info("Fetching consent for process ID: {}, user ID: {}, consentType: {}, locale: {}", processId, userId, consentType, locale);
        return consentText;
    }

    @Override
    public ApproveConsentResponse approveConsent(final ApproveConsentRequest request) {
        final String processId = request.getProcessId();
        final String userId = request.getUserId();
        final String consentType = request.getConsentType();
        final boolean approved = request.isApproved();

        logger.info("Approved consent for process ID: {}, user ID: {}, consentType: {}, approved: {}", processId, userId, consentType, approved);
        return new ApproveConsentResponse();
    }

    @Override
    public Mono<EvaluateClientResponse> evaluateClient(EvaluateClientRequest request) {
        final String processId = request.getProcessId();
        final String userId = request.getUserId();
        final String verificationId = request.getVerificationId();
        final String identityVerificationId = request.getIdentityVerificationId();

        logger.info("Evaluating client for process ID: {}, user ID: {}, verification ID: {}, identityVerification ID: {}",
                processId, userId, verificationId, identityVerificationId);
        return Mono.just(EvaluateClientResponse.builder().accepted(true).build());
    }
}