/*
 * PowerAuth Enrollment Server
 * Copyright (C) 2023 Wultra s.r.o.
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
package com.wultra.app.onboardingserver.impl.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Converter for HTTP request context information.
 *
 * @author Petr Dvorak, petr@wultra.com
 * @author Lubos Racansky, lubos.racansky@wultra.com
 */
// TODO (racansky, 2023-01-09) duplicates com.wultra.app.enrollmentserver.impl.service.converter.RequestContextConverter, extract to lime-java-core
@Component
public class RequestContextConverter {

    /**
     * List of HTTP headers that may contain the actual IP address
     * when hidden behind a proxy component.
     */
    private static final List<String> HTTP_HEADERS_IP_ADDRESS = List.of(
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    );

    private static final String HTTP_HEADER_USER_AGENT = "User-Agent";

    /**
     * Convert HTTP Servlet Request to request context representation.
     *
     * @param source HttpServletRequest instance.
     * @return Request context data.
     */
    public RequestContext convert(HttpServletRequest source) {
        if (source == null) {
            return null;
        }
        final RequestContext destination = new RequestContext();
        destination.setUserAgent(source.getHeader(HTTP_HEADER_USER_AGENT));
        destination.setIpAddress(getClientIpAddress(source));
        return destination;
    }

    /**
     * Obtain the best-effort guess of the client IP address.
     * @param request HttpServletRequest instance.
     * @return Best-effort information about the client IP address.
     */
    private String getClientIpAddress(final HttpServletRequest request) {
        if (request == null) { // safety null check
            return null;
        }
        for (String header : HTTP_HEADERS_IP_ADDRESS) {
            final String ip = request.getHeader(header);
            if (StringUtils.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }
        }
        return request.getRemoteAddr();
    }

}