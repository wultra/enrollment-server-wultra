/*
 * PowerAuth Enrollment Server
 * Copyright (C) 2022 Wultra s.r.o.
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
package com.wultra.app.onboardingserver.common.configuration;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

/**
 * Common onboarding configuration.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 * @author Lubos Racansky, lubos.racansky@wultra.com
 */
@Data
public class CommonOnboardingConfig {

    @Value("${enrollment-server-onboarding.onboarding-process.otp.max-failed-attempts:5}")
    private int otpMaxFailedAttempts;

    @Value("${enrollment-server-onboarding.onboarding-process.max-error-score:15}")
    private int maxProcessErrorScore;

    @Value("${enrollment-server-onboarding.identity-verification.max-failed-attempts:5}")
    private int verificationMaxFailedAttempts;

    @Value("${enrollment-server-onboarding.identity-verification.max-failed-attempts-document-upload:5}")
    private int documentUploadMaxFailedAttempts;

}
