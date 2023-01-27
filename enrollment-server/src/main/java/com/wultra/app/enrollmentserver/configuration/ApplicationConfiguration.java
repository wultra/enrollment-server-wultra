package com.wultra.app.enrollmentserver.configuration;

import io.getlime.security.powerauth.rest.api.spring.application.PowerAuthApplicationConfiguration;
import io.getlime.security.powerauth.rest.api.spring.model.ActivationContext;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration of PowerAuth application which adds activation flags into custom object
 * when obtaining activation status.
 */
@Configuration
public class ApplicationConfiguration implements PowerAuthApplicationConfiguration {

    @Override
    public Map<String, Object> statusServiceCustomObject(ActivationContext context) {
        List<String> activationFlags = context.getActivationFlags();
        Map<String, Object> customObject = new LinkedHashMap<>();
        customObject.put("activationFlags", activationFlags);
        return customObject;
    }
}