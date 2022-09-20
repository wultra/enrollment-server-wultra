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
package com.wultra.app.onboardingserver.statemachine.action.clientevaluation;

import com.wultra.app.enrollmentserver.model.integration.OwnerId;
import com.wultra.app.onboardingserver.common.database.entity.IdentityVerificationEntity;
import com.wultra.app.onboardingserver.impl.service.ClientEvaluationService;
import com.wultra.app.onboardingserver.statemachine.consts.EventHeaderName;
import com.wultra.app.onboardingserver.statemachine.consts.ExtendedStateVariable;
import com.wultra.app.onboardingserver.statemachine.enums.OnboardingEvent;
import com.wultra.app.onboardingserver.statemachine.enums.OnboardingState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Action to initialize client evaluation.
 *
 * @author Lukas Lukovsky, lukas.lukovsky@wultra.com
 */
@Component
public class ClientEvaluationInitAction implements Action<OnboardingState, OnboardingEvent> {

    private final ClientEvaluationService clientEvaluationService;

    @Autowired
    public ClientEvaluationInitAction(ClientEvaluationService clientEvaluationService) {
        this.clientEvaluationService = clientEvaluationService;
    }

    @Override
    public void execute(final StateContext<OnboardingState, OnboardingEvent> context) {
        final OwnerId ownerId = (OwnerId) context.getMessageHeader(EventHeaderName.OWNER_ID);
        final IdentityVerificationEntity identityVerification = context.getExtendedState().get(ExtendedStateVariable.IDENTITY_VERIFICATION, IdentityVerificationEntity.class);

        clientEvaluationService.initClientEvaluation(ownerId, identityVerification);

        sendNextStateEvent(context);
    }

    private static void sendNextStateEvent(final StateContext<OnboardingState, OnboardingEvent> context) {
        final Message<OnboardingEvent> message = MessageBuilder.withPayload(OnboardingEvent.EVENT_NEXT_STATE)
                .setHeader(EventHeaderName.OWNER_ID, context.getMessageHeader(EventHeaderName.OWNER_ID))
                .setHeader(EventHeaderName.PROCESS_ID, context.getMessageHeader(EventHeaderName.PROCESS_ID))
                .build();
        context.getStateMachine().sendEvent(Mono.just(message)).subscribe();
    }
}
