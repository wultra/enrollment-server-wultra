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
package com.wultra.app.onboardingserver.impl.util;

import com.wultra.app.enrollmentserver.model.integration.OwnerId;
import io.getlime.security.powerauth.rest.api.spring.authentication.PowerAuthActivation;
import io.getlime.security.powerauth.rest.api.spring.authentication.PowerAuthApiAuthentication;

/**
 * PowerAuth utilities.
 *
 * @author Lukas Lukovsky, lukas.lukovsky@wultra.com
 */
public final class PowerAuthUtil {

    private PowerAuthUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Provides context data
     * @param apiAuthentication Authentication object with user and app details.
     * @return OwnerId context
     */
    public static OwnerId getOwnerId(PowerAuthApiAuthentication apiAuthentication) {
        PowerAuthActivation powerAuthActivation = apiAuthentication.getActivationContext();

        OwnerId ownerId = new OwnerId();
        ownerId.setActivationId(powerAuthActivation.getActivationId());
        ownerId.setUserId(powerAuthActivation.getUserId());

        return ownerId;
    }

}
