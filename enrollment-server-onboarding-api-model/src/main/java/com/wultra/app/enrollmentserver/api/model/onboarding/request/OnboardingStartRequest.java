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
package com.wultra.app.enrollmentserver.api.model.onboarding.request;

import lombok.Data;

import java.util.Map;

/**
 * Request class used when starting the onboarding process.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Data
public class OnboardingStartRequest {

    private Map<String, Object> identification;

    /**
     * Optional FDS data, structure is vendor specific.
     */
    private Map<String, Object> fdsData;

}
