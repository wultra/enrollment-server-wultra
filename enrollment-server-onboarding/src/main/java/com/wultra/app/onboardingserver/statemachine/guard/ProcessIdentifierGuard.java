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
package com.wultra.app.onboardingserver.statemachine.guard;

import com.wultra.app.enrollmentserver.model.enumeration.OnboardingStatus;
import com.wultra.app.enrollmentserver.model.integration.OwnerId;
import com.wultra.app.onboardingserver.common.database.OnboardingProcessRepository;
import com.wultra.app.onboardingserver.common.database.entity.OnboardingProcessEntity;
import com.wultra.app.onboardingserver.common.errorhandling.OnboardingProcessException;
import com.wultra.app.onboardingserver.statemachine.consts.EventHeaderName;
import com.wultra.app.onboardingserver.statemachine.enums.OnboardingEvent;
import com.wultra.app.onboardingserver.statemachine.enums.OnboardingState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.guard.Guard;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Guard to ensure valid process identifier
 *
 * @author Lukas Lukovsky, lukas.lukovsky@wultra.com
 */
@Component
public class ProcessIdentifierGuard implements Guard<OnboardingState, OnboardingEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ProcessIdentifierGuard.class);

    private final OnboardingProcessRepository onboardingProcessRepository;

    public ProcessIdentifierGuard(OnboardingProcessRepository onboardingProcessRepository) {
        this.onboardingProcessRepository = onboardingProcessRepository;
    }

    /**
     * Verifies process identifier.
     */
    @Override
    public boolean evaluate(StateContext<OnboardingState, OnboardingEvent> context) {
        final OwnerId ownerId = (OwnerId) context.getMessageHeader(EventHeaderName.OWNER_ID);
        final String processId = (String) context.getMessageHeader(EventHeaderName.PROCESS_ID);

        logger.debug("Onboarding process will be locked using PESSIMISTIC_WRITE lock, {}", processId);
        final Optional<OnboardingProcessEntity> processOptional = onboardingProcessRepository.findByActivationIdAndStatusWithLock(ownerId.getActivationId(), OnboardingStatus.VERIFICATION_IN_PROGRESS);
        if (processOptional.isEmpty()) {
            fail(context, processId, ownerId);
            return false;
        }
        final String expectedProcessId = processOptional.get().getId();

        if (!expectedProcessId.equals(processId)) {
            fail(context, processId, ownerId);
            return false;
        }
        return true;
    }

    private void fail(StateContext<OnboardingState, OnboardingEvent> context, String processId, OwnerId ownerId) {
        final Exception ex = new OnboardingProcessException(String.format("Invalid process ID received in request: %s, %s", processId, ownerId));
        context.getStateMachine().setStateMachineError(ex);
    }

}
