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
package com.wultra.app.enrollmentserver.model.integration;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Information about a presence check session.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Data
public class SessionInfo {

    public static final String ATTRIBUTE_TIMESTAMP_LAST_USED = "timestampLastUsed";
    public static final String ATTRIBUTE_IMAGE_UPLOADED = "imageUploaded";
    public static final String ATTRIBUTE_PRIMARY_DOCUMENT_REFERENCE = "primaryDocumentReference";
    public static final String ATTRIBUTE_OTHER_DOCUMENTS_REFERENCES = "otherDocumentsReferences";

    private Map<String, Object> sessionAttributes = new LinkedHashMap<>();

}
