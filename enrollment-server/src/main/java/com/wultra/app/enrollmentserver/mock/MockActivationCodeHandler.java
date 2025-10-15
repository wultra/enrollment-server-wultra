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
import com.wultra.security.powerauth.client.model.entity.Application;
import com.wultra.security.powerauth.client.model.error.PowerAuthClientException;
import com.wultra.security.powerauth.client.model.response.GetApplicationListResponse;
import com.wultra.security.powerauth.client.v3.PowerAuthClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
    public TransferConfigurationResponse fetchTransferConfiguration(final TransferConfigurationRequest request) {
        final String sourceApplicationId = request.sourceApplicationId();
        final String targetApplicationId = request.targetApplicationId();

        logger.info("Destination application ID requested in activation code handler for application: {}, source application ID: {}", targetApplicationId, sourceApplicationId);
        try {
            final GetApplicationListResponse response = powerAuthClient.getApplicationList();
            for (Application application : response.getApplications()) {
                if (application.getApplicationId().equals(targetApplicationId)) {
                    logger.info("Destination application ID was resolved: {}", application.getApplicationId());
                    return TransferConfigurationResponse.builder()
                            .applicationId(application.getApplicationId())
                            .type(ActivationTransferType.SPAWN)
                            .build();
                }
            }
        } catch (PowerAuthClientException ex) {
            logger.error(ex.getMessage(), ex);
        }
        logger.error("Destination application was not found: {}", targetApplicationId);
        return null;
    }
}
