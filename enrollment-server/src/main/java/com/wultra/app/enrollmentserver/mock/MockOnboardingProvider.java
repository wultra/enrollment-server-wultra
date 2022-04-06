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
package com.wultra.app.enrollmentserver.mock;

import com.wultra.app.enrollmentserver.errorhandling.OnboardingProviderException;
import com.wultra.app.enrollmentserver.provider.OnboardingProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Mock onboarding provider for testing purposes.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Component
public class MockOnboardingProvider implements OnboardingProvider {

    private static final Logger logger = LoggerFactory.getLogger(MockOnboardingProvider.class);

    @Override
    public String lookupUser(Map<String, Object> identification) throws OnboardingProviderException {
        logger.info("Lookup user called: {}", identification);
        return "mockuser_" + identification.get("clientId");
    }

    @Override
    public void sendOtpCode(String userId, String otpCode, boolean resend) throws OnboardingProviderException {
        logger.info("Send OTP code called, user ID: {}, OTP code: {}, resend: {}", userId, otpCode, resend);
    }

}