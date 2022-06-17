package com.wultra.app.onboardingserver.mock.model.request;

import com.wultra.app.enrollmentserver.model.enumeration.OtpType;
import lombok.Data;

/**
 * OTP detail request model class.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Data
public class OtpDetailRequest {

    private String processId;
    private OtpType otpType;

}
