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

import com.wultra.app.enrollmentserver.impl.service.DelegatingActivationCodeHandler;
import com.wultra.security.powerauth.client.PowerAuthClient;
import com.wultra.security.powerauth.client.model.entity.Application;
import com.wultra.security.powerauth.client.model.error.PowerAuthClientException;
import com.wultra.security.powerauth.client.model.response.GetApplicationListResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mock activation code handler.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Component
public class MockActivationCodeHandler implements DelegatingActivationCodeHandler {

    private static final Logger logger = LoggerFactory.getLogger(MockActivationCodeHandler.class);

    private final PowerAuthClient powerAuthClient;

    @Autowired
    public MockActivationCodeHandler(PowerAuthClient powerAuthClient) {
        this.powerAuthClient = powerAuthClient;
    }

    @Override
    public String fetchDestinationApplicationId(String applicationId, String sourceAppId, List<String> activationFlags, List<String> applicationRoles) {
        logger.info("Destination application ID requested in activation code handler for application: {}, source application ID: {}", applicationId, sourceAppId);
        try {
            final GetApplicationListResponse response = powerAuthClient.getApplicationList();
            for (Application application : response.getApplications()) {
                if (application.getApplicationId().equals(applicationId)) {
                    logger.info("Destination application ID was resolved: {}", application.getApplicationId());
                    return application.getApplicationId();
                }
            }
        } catch (PowerAuthClientException ex) {
            logger.error(ex.getMessage(), ex);
        }
        logger.error("Destination application was not found: {}", applicationId);
        return null;
    }

    @Override
    public List<String> addActivationFlags(String sourceActivationId, List<String> sourceActivationFlags, String userId, String applicationId, String sourceAppId, List<String> sourceApplicationRoles, String destinationAppId, String destinationActivationId, String activationCode, String activationCodeSignature) {
        logger.info("No activation flags will be added by activation code handler for activation ID: {}", destinationActivationId);
        return null;
    }

    @Override
    public void didReturnActivationCode(String sourceActivationId, String userId, String applicationId, String sourceAppId, String destinationAppId, String destinationActivationId, String activationCode, String activationCodeSignature) {
        logger.info("Activation was successfully created in activation code handler, activation ID: {}, activation code: {}", destinationActivationId, activationCode);
    }
}
