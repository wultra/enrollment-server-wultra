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
package com.wultra.app.enrollmentserver.configuration;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Identity verification configuration.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Configuration
@Data
public class IdentityVerificationConfig {

    @Value("${enrollment-server.document-verification.provider:mock}")
    private String documentVerificationProvider;

    @Value("${enrollment-server.document-verification.verificationOnSubmitEnabled:true}")
    private boolean documentVerificationOnSubmitEnabled;

    @Value("${enrollment-server.document-verification.cleanupEnabled:false}")
    private boolean documentVerificationCleanupEnabled;

    @Value("${enrollment-server.presence-check.enabled:true}")
    private boolean presenceCheckEnabled;

    @Value("${enrollment-server.presence-check.verifySelfieWithDocumentsEnabled:false}")
    private boolean verifySelfieWithDocumentsEnabled;

    @Value("${enrollment-server.presence-check.provider:mock}")
    private String presenceCheckProvider;

    @Value("${enrollment-server.presence-check.cleanupEnabled:false}")
    private boolean presenceCheckCleanupEnabled;

    @Value("${enrollment-server.identity-verification.data-retention.hours:1}")
    private int dataRetentionTime;

    @Value("${enrollment-server.onboarding-process.verification.expiration.seconds:300}")
    private int verificationExpirationTime;

    @Value("${enrollment-server.identity-verification.otp.enabled:true}")
    private boolean verificationOtpEnabled;

}