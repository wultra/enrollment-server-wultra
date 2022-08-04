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
package com.wultra.app.onboardingserver.errorhandling;

/**
 * Exception thrown in case onboarding OTP delivery fails.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
public class OnboardingOtpDeliveryException extends Exception {

    private static final long serialVersionUID = -2641221254167428346L;

    /**
     * No-arg constructor.
     */
    public OnboardingOtpDeliveryException() {
        super();
    }

    /**
     * Constructs a new exception with the specified cause.
     *
     * @param  cause the cause (which is saved for later retrieval by the {@link #getCause()} method).
     */
    public OnboardingOtpDeliveryException(Throwable cause) {
        super(cause);
    }
}
