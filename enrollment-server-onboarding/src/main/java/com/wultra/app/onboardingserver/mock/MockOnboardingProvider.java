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
package com.wultra.app.onboardingserver.mock;

import com.wultra.app.onboardingserver.provider.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.UUID;

/**
 * Mock onboarding provider for testing purposes.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Component
public class MockOnboardingProvider implements OnboardingProvider {

    private static final Logger logger = LoggerFactory.getLogger(MockOnboardingProvider.class);

    @Override
    public LookupUserResponse lookupUser(LookupUserRequest request) {
        logger.info("Lookup user called: {}", request.getIdentification());
        return LookupUserResponse.builder()
                .userId("mockuser_" + request.getIdentification().get("clientId"))
                .consentRequired(true)
                .build();
    }

    @Override
    public void sendOtpCode(SendOtpCodeRequest request) {
        final String userId = request.getUserId();
        final String otpCode = request.getOtpCode();
        final boolean resend = request.isResend();
        logger.info("Send OTP code called, user ID: {}, OTP code: {}, resend: {}", userId, otpCode, resend);
    }

    @Override
    public String fetchConsent(final ConsentTextRequest request) {
        final UUID processId = request.getProcessId();
        final String userId = request.getUserId();
        final String consentType = request.getConsentType();
        final Locale locale = request.getLocale();
        logger.info("Fetching consent for processId: {}, userId: {}, consentType: {}, locale: {}", processId, userId, consentType, locale);

        return "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <style> html {\n" +
                "        background: #F4F4F4;\n" +
                "        font-family: Helvetica, Arial, sans-serif;\n" +
                "        font-size: 16px;\n" +
                "        line-height: 26px;\n" +
                "        font-weight: 300;\n" +
                "    }\n" +
                "\n" +
                "    .logo {\n" +
                "        width: 112px;\n" +
                "        height: 30px;\n" +
                "        margin: 20px auto;\n" +
                "    }\n" +
                "\n" +
                "    h1 {\n" +
                "        color: #000;\n" +
                "        font-size: 19px;\n" +
                "        line-height: 25px;\n" +
                "        font-weight: 400;\n" +
                "        margin-top: 0;\n" +
                "        margin-bottom: 20px;\n" +
                "    }\n" +
                "\n" +
                "    .boxWrapper {\n" +
                "        position: relative;\n" +
                "        margin: 20px;\n" +
                "    }\n" +
                "\n" +
                "    .box {\n" +
                "        padding: 30px 15px;\n" +
                "        text-align: center;\n" +
                "        background: #FFF;\n" +
                "        color: #4d4f53;\n" +
                "    }\n" +
                "\n" +
                "    .box::before {\n" +
                "        background: #fedf00;\n" +
                "        content: \"\";\n" +
                "        position: absolute;\n" +
                "        width: 50px;\n" +
                "        height: 4px;\n" +
                "        top: 0;\n" +
                "        left: 50%;\n" +
                "        transform: translateX(-50%);\n" +
                "    } </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "<div class=\"logo\">\n" +
                "    <svg version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 148.74 40.06\">\n" +
                "        <style type=\"text/css\"> .cls-1 {\n" +
                "            fill: #ffed00;\n" +
                "        }\n" +
                "\n" +
                "        .cls-2 {\n" +
                "            fill: #1d1d1b;\n" +
                "        } </style>\n" +
                "        <g data-name=\"Vrstva 2\">\n" +
                "            <g data-name=\"Vrstva 1\">\n" +
                "                <g>\n" +
                "                    <rect x=\"0.37\" y=\"2.73\" width=\"37.33\" height=\"37.34\" class=\"cls-1\"></rect>\n" +
                "                    <g>\n" +
                "                        <path d=\"M52.78,11A4.22,4.22,0,0,0,56.2,6.6c0-3.34-2.34-4.81-5.44-4.81H44.68V17.44h4.07v-6h0l3.84,6H57.7Zm-4-6.13h.39c1.33,0,2.83.25,2.83,1.95s-1.5,2-2.83,2h-.39ZM70.36,6.79H66.58v1a4.32,4.32,0,0,0-3.3-1.39A5.42,5.42,0,0,0,58,12.1a5.47,5.47,0,0,0,5.37,5.69,3.73,3.73,0,0,0,3.22-1.53h0v1.18h3.78ZM64.28,9.72a2.31,2.31,0,0,1,2.43,2.41,2.43,2.43,0,0,1-4.86,0,2.31,2.31,0,0,1,2.43-2.41m12.6-7A2.12,2.12,0,1,1,74.76.58,2.13,2.13,0,0,1,76.88,2.7m-4,4.09h3.78V17.44H72.87ZM83.3,17.44H79.52V9.9H78.19V6.79h1.33V5.44A4.89,4.89,0,0,1,80.6,1.7,4.82,4.82,0,0,1,84.31,0a3.23,3.23,0,0,1,1.62.38V3.63a3.32,3.32,0,0,0-1.33-.35c-1,0-1.3.58-1.3,1.79V6.79h2.63V9.9H83.3Zm8,0H87.57V9.9H86.24V6.79h1.33V5.44A4.93,4.93,0,0,1,88.65,1.7,4.86,4.86,0,0,1,92.37,0,3.28,3.28,0,0,1,94,.38V3.63a3.37,3.37,0,0,0-1.33-.35c-1,0-1.31.58-1.31,1.79V6.79H94V9.9H91.35Zm14.85-5.09c0-3.63-2.14-5.91-5.84-5.91-3.47,0-5.89,2.14-5.89,5.69s2.63,5.66,6.16,5.66c2.43,0,4.82-1.14,5.42-3.65h-3.63a1.89,1.89,0,0,1-1.81,1,2.11,2.11,0,0,1-2.36-2.36h8Zm-7.89-1.78a2.27,2.27,0,0,1,4.38,0ZM111.93,2.7a2.12,2.12,0,0,1-4.24,0,2.12,2.12,0,0,1,4.24,0m-4,4.09h3.78V17.44h-3.78Zm13.31,2.74a4.71,4.71,0,0,0-2-.52c-.43,0-1.18.19-1.18.73,0,.72,1.18.83,1.7.93,1.77.36,3.1,1.18,3.1,3.16,0,2.8-2.56,4-5,4a8.27,8.27,0,0,1-4.3-1.22l1.37-2.64a6.1,6.1,0,0,0,3,1.08c.48,0,1.19-.23,1.19-.81,0-.83-1.19-.87-2.37-1.18a2.67,2.67,0,0,1-2.37-2.81c0-2.61,2.37-3.77,4.7-3.77a8.45,8.45,0,0,1,3.51.68Zm14.55,2.82c0-3.63-2.14-5.91-5.83-5.91-3.47,0-5.9,2.14-5.9,5.69s2.64,5.66,6.17,5.66c2.43,0,4.82-1.14,5.42-3.65H132a1.86,1.86,0,0,1-1.8,1,2.11,2.11,0,0,1-2.37-2.36h8Zm-7.89-1.78a2.14,2.14,0,0,1,2.25-1.68,2.11,2.11,0,0,1,2.13,1.68Zm13.39-2.41h.05a3.67,3.67,0,0,1,3.4-1.72c2.87,0,4,1.8,4,4.42v6.58H145V12.25c0-1,.17-2.8-1.7-2.8-1.54,0-2,1.14-2,2.47v5.52h-3.77V6.79h3.77ZM50.86,27.5h.29c1.1,0,2.36.09,2.13,1.52s-1.36,1.52-2.44,1.52h-.46Zm.22,12.56c2.87,0,5.84-1.08,6.36-4.36.35-2.19-.37-3.7-2.57-4v0a3.91,3.91,0,0,0,2.23-3.2c.48-3-1.22-4.07-4-4.07H47.28L44.8,40.06ZM49.9,33.57h.42c1.33,0,3.37-.18,3.07,1.71-.27,1.73-2.3,1.66-3.5,1.66h-.52Zm19,3.77.59,2.72h4.34L70.42,24.39H66L57.46,40.06h4.32l1.52-2.72Zm-.61-3.12H64.91l2.48-5h0Zm9.29-9.83h4.08L87.61,34h0l1.51-9.58h4.08L90.77,40.06H86.69l-5.94-9.6h0l-1.52,9.6H75.11Zm27.37,0H110l-7.42,7.42,5.49,8.25h-5.27l-4.19-6.79h0L97.5,40.06H93.43l2.48-15.67H100l-1,6.46h0Z\"\n" +
                "                              class=\"cls-2\"></path>\n" +
                "                        <path d=\"M0,1.78H38.24V40H0ZM1,39H37.16V2.86H1Zm18-13.47L30.57,37l4.51-4.5L23.55,21l2.25-2.23V14.27l1.6-1.6v3.71L29,17.93l5.62-5.63,1.52,1.5a10.07,10.07,0,0,0-3-6.9,4.81,4.81,0,0,0-7-.18L20.7,12.15l1.35,1.35-3,3-3-3,1.35-1.35L12,6.72A4.82,4.82,0,0,0,5,6.9a9.94,9.94,0,0,0-2.89,6.9l1.46-1.5,5.61,5.63,1.57-1.56v-3.7l1.6,1.6v4.47L14.57,21,3.16,32.38l4.5,4.49Z\"\n" +
                "                              class=\"cls-2\"></path>\n" +
                "                    </g>\n" +
                "                </g>\n" +
                "            </g>\n" +
                "        </g>\n" +
                "    </svg>\n" +
                "</div>\n" +
                "<div class=\"boxWrapper\">\n" +
                "    <div class=\"box\"><h1>Zpracování údajů</h1> Souhlasím, aby Raiffeisenbank a.s. zpracovávala biometrické údaje odvozené ze záznamu mé podoby\n" +
                "        pořízené při reaktivaci RB klíče, a to za účelem ověření shody mé podoby s údaji získanými z fotografií na poskytnutých kopiích dokladů.\n" +
                "    </div>\n" +
                "</div>\n" +
                "</body>\n" +
                "</html>";
    }

    @Override
    public ApproveConsentResponse approveConsent(final ApproveConsentRequest request) {
        final UUID processId = request.getProcessId();
        final String userId = request.getUserId();
        final String consentType = request.getConsentType();
        final Boolean approved = request.getApproved();

        logger.info("Approved consent for processId: {}, userId: {}, consentType: {}, approved: {}", processId, userId, consentType, approved);
        return new ApproveConsentResponse();
    }
}