/*
 * PowerAuth Enrollment Server
 * Copyright (C) 2022 Wultra s.r.o.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.wultra.app.onboardingserver.statemachine.action.verification;

import com.wultra.app.enrollmentserver.model.integration.OwnerId;
import com.wultra.app.onboardingserver.common.database.entity.IdentityVerificationEntity;
import com.wultra.app.onboardingserver.common.errorhandling.RemoteCommunicationException;
import com.wultra.app.onboardingserver.api.errorhandling.DocumentVerificationException;
import com.wultra.app.onboardingserver.impl.service.IdentityVerificationService;
import com.wultra.app.onboardingserver.statemachine.consts.EventHeaderName;
import com.wultra.app.onboardingserver.statemachine.consts.ExtendedStateVariable;
import com.wultra.app.onboardingserver.statemachine.enums.OnboardingEvent;
import com.wultra.app.onboardingserver.statemachine.enums.OnboardingState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import static com.wultra.app.enrollmentserver.model.enumeration.IdentityVerificationPhase.DOCUMENT_VERIFICATION;
import static com.wultra.app.enrollmentserver.model.enumeration.IdentityVerificationStatus.FAILED;

/**
 * Action to start the verification process
 *
 * @author Lukas Lukovsky, lukas.lukovsky@wultra.com
 */
@Component
public class VerificationDocumentStartAction implements Action<OnboardingState, OnboardingEvent> {

    private static final Logger logger = LoggerFactory.getLogger(VerificationDocumentStartAction.class);

    private final IdentityVerificationService identityVerificationService;

    @Autowired
    public VerificationDocumentStartAction(IdentityVerificationService identityVerificationService) {
        this.identityVerificationService = identityVerificationService;
    }

    @Override
    public void execute(StateContext<OnboardingState, OnboardingEvent> context) {
        OwnerId ownerId = (OwnerId) context.getMessageHeader(EventHeaderName.OWNER_ID);
        IdentityVerificationEntity identityVerification = context.getExtendedState().get(ExtendedStateVariable.IDENTITY_VERIFICATION, IdentityVerificationEntity.class);

        try {
            identityVerificationService.startVerification(ownerId, identityVerification);
            logger.info("Started document verification process of {}", identityVerification);
        } catch (DocumentVerificationException | RemoteCommunicationException e) {
            identityVerificationService.moveToPhaseAndStatus(identityVerification, DOCUMENT_VERIFICATION, FAILED, ownerId);
            logger.warn("Verification start failed, {}", ownerId, e);
        }
    }

}
