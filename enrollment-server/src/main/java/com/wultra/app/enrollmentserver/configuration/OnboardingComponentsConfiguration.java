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
package com.wultra.app.enrollmentserver.configuration;

import com.wultra.app.onboardingserver.common.activation.ActivationOtpService;
import com.wultra.app.onboardingserver.common.activation.ActivationProcessService;
import com.wultra.app.onboardingserver.common.api.OnboardingService;
import com.wultra.app.onboardingserver.common.api.OtpService;
import com.wultra.app.onboardingserver.common.configuration.CommonOnboardingConfig;
import com.wultra.app.onboardingserver.common.database.OnboardingOtpRepository;
import com.wultra.app.onboardingserver.common.database.OnboardingProcessRepository;
import com.wultra.app.onboardingserver.common.service.CommonOnboardingService;
import com.wultra.app.onboardingserver.common.service.CommonOtpService;
import com.wultra.app.onboardingserver.common.service.CommonProcessLimitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Configuration of common components.
 *
 * @author Lubos Racansky, lubos.racansky@wultra.com
 */
@Configuration
@EnableJpaRepositories(basePackages = {
        "com.wultra.app.enrollmentserver.database", // not to override component scan for enrollment-server
        "com.wultra.app.onboardingserver.common.database" // dependencies from enrollment-server-onboarding common
})
@EntityScan(basePackages = {
        "com.wultra.app.enrollmentserver.database.entity", // not to override component scan for enrollment-server
        "com.wultra.app.onboardingserver.common.database.entity" // dependencies from enrollment-server-onboarding common
})
public class OnboardingComponentsConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(OnboardingComponentsConfiguration.class);

    /**
     * No-arg constructor.
     */
    public OnboardingComponentsConfiguration() {
        logger.info("Onboarding feature turned on, configuring enrollment components specific for onboarding.");
    }

    /**
     * Register onboarding service bean.
     *
     * @param onboardingProcessRepository onboarding process repository
     * @return onboarding service bean
     */
    @Bean
    public OnboardingService onboardingService(final OnboardingProcessRepository onboardingProcessRepository) {
        return new CommonOnboardingService(onboardingProcessRepository);
    }

    /**
     * Register otp service bean.
     *
     * @param onboardingOtpRepository onboarding otp repository
     * @param onboardingProcessRepository onboarding process repository
     * @param onboardingConfig onboarding config
     * @return otp service bean
     */
    @Bean
    public OtpService otpService(
            final OnboardingOtpRepository onboardingOtpRepository,
            final OnboardingProcessRepository onboardingProcessRepository,
            final CommonOnboardingConfig onboardingConfig,
            final CommonProcessLimitService commonProcessLimitService) {

        return new CommonOtpService(onboardingOtpRepository, onboardingProcessRepository, onboardingConfig, commonProcessLimitService);
    }

    /**
     * Register onboarding config bean.
     *
     * @return onboarding config bean
     */
    @Bean
    public CommonOnboardingConfig onboardingConfig() {
        return new CommonOnboardingConfig();
    }

    /**
     * Register process limit service.
     * @param config
     * @param onboardingProcessRepository
     * @return
     */
    @Bean
    public CommonProcessLimitService processLimitService(final CommonOnboardingConfig config, final OnboardingProcessRepository onboardingProcessRepository) {
        return new CommonProcessLimitService(config, onboardingProcessRepository);
    }

    /**
     * Register activation otp service bean.
     *
     * @param otpService otp service
     * @return activation otp service bean
     */
    @Bean
    public ActivationOtpService activationOtpService(final OtpService otpService) {
        return new ActivationOtpService(otpService);
    }

    /**
     * Register activation process service bean.
     *
     * @param onboardingService onboading service
     * @return activation process service bean
     */
    @Bean
    public ActivationProcessService activationProcessService(final OnboardingService onboardingService) {
        return new ActivationProcessService(onboardingService);
    }
}
