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
package com.wultra.app.onboardingserver.common.errorhandling;

import java.io.Serial;

/**
 * Exception thrown in case onboarding process fails.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class OnboardingProcessException extends Exception {

    @Serial
    private static final long serialVersionUID = 7558022671624330227L;

    public OnboardingProcessException() {
    }

    public OnboardingProcessException(String message) {
        super(message);
    }

    public OnboardingProcessException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
