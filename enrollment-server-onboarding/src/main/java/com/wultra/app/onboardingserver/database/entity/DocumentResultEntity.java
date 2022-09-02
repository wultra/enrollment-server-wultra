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

package com.wultra.app.onboardingserver.database.entity;

import com.wultra.app.enrollmentserver.model.enumeration.DocumentProcessingPhase;
import com.wultra.app.enrollmentserver.model.enumeration.ErrorOrigin;
import com.wultra.app.enrollmentserver.model.enumeration.RejectOrigin;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * Entity representing a document verification record.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 */
@Getter
@Setter
@ToString(of = {"id", "phase", "documentVerification"})
@NoArgsConstructor
@Entity
@Table(name = "es_document_result")
public class DocumentResultEntity implements Serializable {

    private static final long serialVersionUID = -760284276164288362L;

    /**
     * Autogenerated identifier
     */
    @Id
    @SequenceGenerator(name = "es_document_result", sequenceName = "es_document_result_seq", allocationSize = 10)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "es_document_result")
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * Identifier of the related document verification entity
     */
    @ManyToOne
    @JoinColumn(name = "document_verification_id", referencedColumnName = "id", nullable = false)
    private DocumentVerificationEntity documentVerification;

    /**
     * Phase of processing
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "phase", nullable = false)
    private DocumentProcessingPhase phase;

    /**
     * Reason why the document was rejected
     */
    @Column(name = "reject_reason")
    private String rejectReason;

    /**
     * Origin of rejection
     */
    @Column(name = "reject_origin")
    @Enumerated(EnumType.STRING)
    private RejectOrigin rejectOrigin;

    /**
     * JSON serialized document with the verification result
     */
    @Column(name = "verification_result")
    private String verificationResult;

    /**
     * JSON serialized errors which occurred during document processing
     */
    @Column(name = "error_detail")
    private String errorDetail;

    @Column(name = "error_origin")
    @Enumerated(EnumType.STRING)
    private ErrorOrigin errorOrigin;

    /**
     * JSON serialized data extracted from the uploaded document
     */
    @Column(name = "extracted_data")
    private String extractedData;

    /**
     * Timestamp when the entity was created
     */
    @Column(name = "timestamp_created", nullable = false)
    private Date timestampCreated;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DocumentResultEntity)) return false;
        DocumentResultEntity that = (DocumentResultEntity) o;
        return documentVerification.equals(that.documentVerification) && phase == that.phase && timestampCreated.equals(that.timestampCreated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentVerification, phase, timestampCreated);
    }

}
