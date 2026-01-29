/*
 * PowerAuth Enrollment Server
 * Copyright (C) 2026 Wultra s.r.o.
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
package com.wultra.app.onboardingserver.controller.api;

import com.wultra.app.onboardingserver.common.database.IdentityVerificationRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Temporary controller for testing purposes.
 * Should be removed in the future when we introduce more clever mocks.
 *
 * @author Lubos Racansky, lubos.racansky@wultra.com
 */
@RestController
@RequestMapping(value = "/api/private/test")
@AllArgsConstructor
@Slf4j
class PrivateTestController {

    private final IdentityVerificationRepository identityVerificationRepository;

    /**
     * Map process ID to identity verification IDs, filter not completed ones.
     *
     * @param processId Process ID.
     * @return identity verification ID
     */
    @GetMapping("process/{processId}/identityVerifications")
    public List<String> identityVerifications(final @PathVariable String processId) {
        logger.info("action: identityVerifications, state: initiated, processId: {}", processId);

        final List<String> result = identityVerificationRepository.findNotCompletedIdentityVerifications(List.of(processId));

        logger.info("action: identityVerifications, state: succeeded");

        return result;
    }
}
