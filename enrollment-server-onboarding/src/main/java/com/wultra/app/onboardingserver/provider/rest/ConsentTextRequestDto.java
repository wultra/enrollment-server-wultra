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
package com.wultra.app.onboardingserver.provider.rest;

import lombok.Data;

/**
 * Response object for consent text.
 *
 * @author Lubos Racansky, lubos.racansky@wultra.com
 */
@Data
class ConsentTextRequestDto {

    private String processId;

    private String userId;

    /**
     * Language in ISO 3166-1 alpha-2 format lower cased.
     */
    private String language;

    private String consentType;

}
