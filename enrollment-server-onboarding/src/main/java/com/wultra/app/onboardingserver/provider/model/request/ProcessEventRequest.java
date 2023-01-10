/*
 * PowerAuth Enrollment Server
 * Copyright (C) 2023 Wultra s.r.o.
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
 *
 */
package com.wultra.app.onboardingserver.provider.model.request;

import com.wultra.app.onboardingserver.common.annotation.PublicApi;
import com.wultra.app.onboardingserver.provider.OnboardingProvider;
import lombok.*;

import java.util.Locale;

/**
 * Request object for {@link OnboardingProvider#processEvent(ProcessEventRequest)}.
 *
 * @author Lubos Racansky, lubos.racansky@wultra.com
 */
@Builder
@Getter
@ToString
@PublicApi
@EqualsAndHashCode
public final class ProcessEventRequest {

    @NonNull
    private String processId;

    @NonNull
    private String userId;

    @NonNull
    private String identityVerificationId;

    @NonNull
    private EventType type;

    @NonNull
    private Locale locale;

    public enum EventType {
        FINISHED
    }
}
