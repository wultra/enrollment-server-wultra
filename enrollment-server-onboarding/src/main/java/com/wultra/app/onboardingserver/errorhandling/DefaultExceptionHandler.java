/*
 * PowerAuth Enrollment Server
 * Copyright (C) 2020 Wultra s.r.o.
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

package com.wultra.app.onboardingserver.errorhandling;

import io.getlime.core.rest.model.base.response.ErrorResponse;
import io.getlime.security.powerauth.rest.api.spring.exception.PowerAuthAuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;

/**
 * Exception handler for RESTful API issues.
 *
 * @author Petr Dvorak, petr@wultra.com
 */
@ControllerAdvice
public class DefaultExceptionHandler {

    private final static Logger logger = LoggerFactory.getLogger(DefaultExceptionHandler.class);

    /**
     * Default exception handler, for unexpected errors.
     * @param t Throwable.
     * @return Response with error details.
     */
    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody ErrorResponse handleDefaultException(Throwable t) {
        logger.error("Error occurred when processing the request.", t);
        return new ErrorResponse("ERROR_GENERIC", "Unknown error occurred while processing request.");
    }

    /**
     * Exception handler for invalid request exception.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(InvalidRequestObjectException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleInvalidRequestException(InvalidRequestObjectException ex) {
        logger.warn("Error occurred when registering to push server.", ex);
        return new ErrorResponse("INVALID_REQUEST", "Invalid request object sent to Mobile Token API component.");
    }

    /**
     * Exception handler for invalid request exception.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(PushRegistrationFailedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handlePushRegistrationException(PushRegistrationFailedException ex) {
        logger.warn("Error occurred when registering to push server.", ex);
        return new ErrorResponse("PUSH_REGISTRATION_FAILED", "Push registration failed in Mobile Token API component.");
    }

    /**
     * Handling of unauthorized exception.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(PowerAuthAuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public @ResponseBody ErrorResponse handleUnauthorizedException(PowerAuthAuthenticationException ex) {
        logger.warn("Unable to verify device registration - authentication failed.", ex);
        return new ErrorResponse("POWERAUTH_AUTH_FAIL", "Unable to verify device registration.");
    }

    /**
     * Handling of presence check exceptions.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(PresenceCheckException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handlePresenceCheckException(PresenceCheckException ex) {
        logger.warn("Presence check failed", ex);
        return new ErrorResponse("PRESENCE_CHECK_FAILED", "Presence check failed.");
    }

    /**
     * Handling of identity verification exceptions.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(IdentityVerificationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleIdentityVerificationException(IdentityVerificationException ex) {
        logger.warn("Identity verification failed", ex);
        return new ErrorResponse("IDENTITY_VERIFICATION_FAILED", "Identity verification failed.");
    }

    /**
     * Handling of not enabled presence check exceptions.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(PresenceCheckNotEnabledException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handlePresenceCheckNotEnabledException(
            PresenceCheckNotEnabledException ex, HttpServletRequest request) {
        logger.warn("Calling a service on a not enabled presence check service, requestUri: {}", request.getRequestURI(), ex);
        return new ErrorResponse("PRESENCE_CHECK_NOT_ENABLED", "Presence check is not enabled.");
    }

    /**
     * Handling of document verification exceptions.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(DocumentVerificationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleDocumentVerificationException(DocumentVerificationException ex) {
        logger.warn("Document verification failed", ex);
        return new ErrorResponse("DOCUMENT_VERIFICATION_FAILED", "Document verification failed.");
    }

    /**
     * Handling of document verification exceptions.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(RemoteCommunicationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleRemoteExceptionException(RemoteCommunicationException ex) {
        logger.warn("Communication with remote system failed", ex);
        return new ErrorResponse("REMOTE_COMMUNICATION_ERROR", "Communication with remote system failed.");
    }

    /**
     * Handling of onboarding process exceptions.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(OnboardingProcessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleOnboardingProcessException(OnboardingProcessException ex) {
        logger.warn("Onboarding process failed", ex);
        return new ErrorResponse("ONBOARDING_FAILED", "Onboarding process failed.");
    }

    /**
     * Handling of onboarding OTP delivery exceptions.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(OnboardingOtpDeliveryException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse handleOnboardingOtpDeliveryException(OnboardingOtpDeliveryException ex) {
        logger.warn("Onboarding OTP delivery failed", ex);
        return new ErrorResponse("ONBOARDING_OTP_FAILED", "Onboarding OTP delivery failed.");
    }

    /**
     * Handling of too many onboarding processes exceptions.
     * @param ex Exception.
     * @return Response with error details.
     */
    @ExceptionHandler(TooManyProcessesException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public @ResponseBody ErrorResponse handleTooManyProcessesException(TooManyProcessesException ex) {
        logger.warn("Too many onboarding processes started by the user", ex);
        return new ErrorResponse("TOO_MANY_REQUESTS", "Too many onboarding processes started by the user.");
    }
}