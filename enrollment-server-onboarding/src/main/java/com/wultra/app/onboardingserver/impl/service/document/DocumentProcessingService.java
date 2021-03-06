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
package com.wultra.app.onboardingserver.impl.service.document;

import com.wultra.app.enrollmentserver.api.model.onboarding.request.DocumentSubmitRequest;
import com.wultra.app.enrollmentserver.model.Document;
import com.wultra.app.enrollmentserver.model.DocumentMetadata;
import com.wultra.app.enrollmentserver.model.enumeration.DocumentProcessingPhase;
import com.wultra.app.enrollmentserver.model.enumeration.DocumentStatus;
import com.wultra.app.enrollmentserver.model.enumeration.DocumentType;
import com.wultra.app.enrollmentserver.model.integration.*;
import com.wultra.app.onboardingserver.configuration.IdentityVerificationConfig;
import com.wultra.app.onboardingserver.database.DocumentDataRepository;
import com.wultra.app.onboardingserver.database.DocumentResultRepository;
import com.wultra.app.onboardingserver.database.DocumentVerificationRepository;
import com.wultra.app.onboardingserver.database.entity.DocumentDataEntity;
import com.wultra.app.onboardingserver.database.entity.DocumentResultEntity;
import com.wultra.app.onboardingserver.database.entity.DocumentVerificationEntity;
import com.wultra.app.onboardingserver.database.entity.IdentityVerificationEntity;
import com.wultra.app.onboardingserver.errorhandling.DocumentSubmitException;
import com.wultra.app.onboardingserver.errorhandling.DocumentVerificationException;
import com.wultra.app.onboardingserver.impl.service.DataExtractionService;
import com.wultra.app.onboardingserver.provider.DocumentVerificationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Service implementing document processing features.
 *
 * @author Lukas Lukovsky, lukas.lukovsky@wultra.com
 */
@Service
public class DocumentProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentProcessingService.class);

    private final IdentityVerificationConfig identityVerificationConfig;

    private final DocumentDataRepository documentDataRepository;

    private final DocumentVerificationRepository documentVerificationRepository;

    private final DocumentResultRepository documentResultRepository;

    private final DataExtractionService dataExtractionService;

    private final DocumentVerificationProvider documentVerificationProvider;

    /**
     * Service constructor.
     * @param identityVerificationConfig Identity verification configuration.
     * @param documentDataRepository Document data repository.
     * @param documentVerificationRepository Document verification repository.
     * @param documentResultRepository Document verification result repository.
     * @param dataExtractionService Data extraction service.
     * @param documentVerificationProvider Document verification provider.
     */
    @Autowired
    public DocumentProcessingService(
            IdentityVerificationConfig identityVerificationConfig,
            DocumentDataRepository documentDataRepository,
            DocumentVerificationRepository documentVerificationRepository,
            DocumentResultRepository documentResultRepository,
            DataExtractionService dataExtractionService,
            DocumentVerificationProvider documentVerificationProvider) {
        this.identityVerificationConfig = identityVerificationConfig;
        this.documentDataRepository = documentDataRepository;
        this.documentVerificationRepository = documentVerificationRepository;
        this.documentResultRepository = documentResultRepository;
        this.dataExtractionService = dataExtractionService;
        this.documentVerificationProvider = documentVerificationProvider;
    }

    /**
     * Submit identity-related documents for verification.
     * @param idVerification Identity verification entity.
     * @param request Document submit request.
     * @param ownerId Owner identification.
     * @return Document verification entities.
     */
    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public List<DocumentVerificationEntity> submitDocuments(
            IdentityVerificationEntity idVerification,
            DocumentSubmitRequest request,
            OwnerId ownerId) throws DocumentSubmitException {

        List<Document> documents = getDocuments(ownerId, request);

        List<DocumentVerificationEntity> docVerifications = new ArrayList<>();
        List<DocumentResultEntity> docResults = new ArrayList<>();

        List<DocumentSubmitRequest.DocumentMetadata> docsMetadata = request.getDocuments();
        for (DocumentSubmitRequest.DocumentMetadata docMetadata : docsMetadata) {
            DocumentVerificationEntity docVerification = createDocumentVerification(ownerId, idVerification, docMetadata);
            docVerification.setIdentityVerification(idVerification);
            docVerifications.add(docVerification);

            checkDocumentResubmit(ownerId, request, docVerification);

            SubmittedDocument submittedDoc;
            try {
                submittedDoc = createSubmittedDocument(ownerId, docMetadata, documents);
            } catch (DocumentSubmitException e) {
                docVerification.setStatus(DocumentStatus.FAILED);
                docVerification.setErrorDetail(e.getMessage());
                return docVerifications;
            }

            DocumentSubmitResult docSubmitResult = submitDocumentToProvider(ownerId, docVerification, submittedDoc);

            DocumentResultEntity docResult = createDocumentResult(docVerification, docSubmitResult);
            docResult.setTimestampCreated(ownerId.getTimestamp());

            docResults.add(docResult);

            // Delete previously persisted document after a successful upload to the provider
            if (docVerification.getUploadId() != null && docMetadata.getUploadId() != null) {
                documentDataRepository.deleteById(docMetadata.getUploadId());
            }
        }

        documentVerificationRepository.saveAll(docVerifications);

        for (int i = 0; i < docVerifications.size(); i++) {
            DocumentVerificationEntity docVerificationEntity = docVerifications.get(i);
            docResults.get(i).setDocumentVerification(docVerificationEntity);
        }
        documentResultRepository.saveAll(docResults);

        return docVerifications;
    }

    public void checkDocumentResubmit(OwnerId ownerId,
                                      DocumentSubmitRequest request,
                                      DocumentVerificationEntity docVerification) throws DocumentSubmitException {
        if (request.isResubmit() && docVerification.getOriginalDocumentId() == null) {
            logger.error("Detected a resubmit request without specified originalDocumentId for {}, {}", docVerification, ownerId);
            throw new DocumentSubmitException("Missing originalDocumentId in a resubmit request");
        } else if (request.isResubmit()) {
            Optional<DocumentVerificationEntity> originalDocOptional =
                    documentVerificationRepository.findById(docVerification.getOriginalDocumentId());
            if (originalDocOptional.isEmpty()) {
                logger.warn("Missing previous DocumentVerificationEntity(originalDocumentId={}), {}",
                        docVerification.getOriginalDocumentId(), ownerId);
            } else {
                DocumentVerificationEntity originalDoc = originalDocOptional.get();
                originalDoc.setStatus(DocumentStatus.DISPOSED);
                originalDoc.setUsedForVerification(false);
                originalDoc.setTimestampDisposed(ownerId.getTimestamp());
                originalDoc.setTimestampLastUpdated(ownerId.getTimestamp());
                logger.info("Replaced previous {} with new {}, {}", originalDocOptional, docVerification, ownerId);
            }
        } else if (!request.isResubmit() && docVerification.getOriginalDocumentId() != null) {
            logger.error("Detected a submit request with specified originalDocumentId={} for {}, {}",
                    docVerification.getOriginalDocumentId(), docVerification, ownerId);
            throw new DocumentSubmitException("Specified originalDocumentId in a submit request");
        }
    }

    /**
     * Checks document submit status and data at the provider.
     * @param ownerId Owner identification.
     * @param documentResultEntity Document result entity to be checked at the provider.
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void checkDocumentSubmitWithProvider(OwnerId ownerId, DocumentResultEntity documentResultEntity) {
        DocumentVerificationEntity docVerification = documentResultEntity.getDocumentVerification();
        DocumentsSubmitResult docsSubmitResults;
        DocumentSubmitResult docSubmitResult;
        try {
            docsSubmitResults = documentVerificationProvider.checkDocumentUpload(ownerId, docVerification);
            docSubmitResult = docsSubmitResults.getResults().get(0);
        } catch (DocumentVerificationException e) {
            docsSubmitResults = new DocumentsSubmitResult();
            docSubmitResult = new DocumentSubmitResult();
            docSubmitResult.setErrorDetail(e.getMessage());
        }

        documentResultEntity.setErrorDetail(docSubmitResult.getErrorDetail());
        documentResultEntity.setExtractedData(docSubmitResult.getExtractedData());
        documentResultEntity.setRejectReason(docSubmitResult.getRejectReason());
        processDocsSubmitResults(ownerId, docVerification, docsSubmitResults, docSubmitResult);
    }

    public DocumentSubmitResult submitDocumentToProvider(OwnerId ownerId, DocumentVerificationEntity docVerification, SubmittedDocument submittedDoc) {
        DocumentsSubmitResult docsSubmitResults;
        DocumentSubmitResult docSubmitResult;
        try {
            docsSubmitResults = documentVerificationProvider.submitDocuments(ownerId, List.of(submittedDoc));
            docSubmitResult = docsSubmitResults.getResults().get(0);
        } catch (DocumentVerificationException e) {
            docsSubmitResults = new DocumentsSubmitResult();
            docSubmitResult = new DocumentSubmitResult();
            docSubmitResult.setErrorDetail(e.getMessage());
        }

        processDocsSubmitResults(ownerId, docVerification, docsSubmitResults, docSubmitResult);
        return docSubmitResult;
    }

    /**
     * Upload a single document related to identity verification.
     * @param idVerification Identity verification entity.
     * @param requestData Binary document data.
     * @param ownerId Owner identification.
     * @return Persisted document metadata of the uploaded document.
     * @throws DocumentVerificationException Thrown when document is invalid.
     */
    @Transactional
    public DocumentMetadata uploadDocument(IdentityVerificationEntity idVerification, byte[] requestData, OwnerId ownerId) throws DocumentVerificationException {
        // TODO consider limiting the amount (count, space) of currently uploaded documents per ownerId
        Document document = dataExtractionService.extractDocument(requestData);
        return persistDocumentData(idVerification, ownerId, document);
    }

    /**
     * Pairs documents with two sides, front side will be linked to the back side and vice versa.
     * @param documents Documents to be checked on two sides linkin
     */
    @Transactional
    public void pairTwoSidedDocuments(List<DocumentVerificationEntity> documents) {
        for (DocumentVerificationEntity document : documents) {
            if (!document.getType().isTwoSided()) {
                continue;
            }
            documents.stream()
                    .filter(item -> item.getType().equals(document.getType()))
                    .filter(item -> !item.getSide().equals(document.getSide()))
                    .forEach(item -> {
                        logger.debug("Found other side {} for {}", item, document);
                        item.setOtherSideId(document.getId());
                        documentVerificationRepository.setOtherDocumentSide(item.getId(), document.getId());
                    });
        }
    }

    /**
     * Persist a document into database.
     * @param idVerification Identity verification entity.
     * @param ownerId Owner identification
     * @param document Document to be persisted.
     * @return Persisted document metadata.
     */
    private DocumentMetadata persistDocumentData(IdentityVerificationEntity idVerification, OwnerId ownerId, Document document) {
        DocumentDataEntity entity = new DocumentDataEntity();
        entity.setActivationId(ownerId.getActivationId());
        entity.setIdentityVerification(idVerification);
        entity.setFilename(document.getFilename());
        entity.setData(document.getData());
        entity.setTimestampCreated(ownerId.getTimestamp());
        entity = documentDataRepository.save(entity);

        document.setId(entity.getId());

        // Return document metadata only
        DocumentMetadata persistedDocument = new DocumentMetadata();
        persistedDocument.setId(entity.getId());
        persistedDocument.setFilename(entity.getFilename());
        return persistedDocument;
    }

    private DocumentResultEntity createDocumentResult(
            DocumentVerificationEntity docVerificationEntity,
            DocumentSubmitResult docSubmitResult) {
        DocumentResultEntity entity = new DocumentResultEntity();
        entity.setErrorDetail(docVerificationEntity.getErrorDetail());
        entity.setExtractedData(docSubmitResult.getExtractedData());
        entity.setPhase(DocumentProcessingPhase.UPLOAD);
        entity.setRejectReason(docVerificationEntity.getRejectReason());
        return entity;
    }

    private DocumentVerificationEntity createDocumentVerification(OwnerId ownerId, IdentityVerificationEntity identityVerification, DocumentSubmitRequest.DocumentMetadata docMetadata) {
        DocumentVerificationEntity entity = new DocumentVerificationEntity();
        entity.setActivationId(ownerId.getActivationId());
        entity.setIdentityVerification(identityVerification);
        entity.setFilename(docMetadata.getFilename());
        entity.setOriginalDocumentId(docMetadata.getOriginalDocumentId());
        entity.setSide(docMetadata.getSide());
        entity.setType(docMetadata.getType());
        entity.setStatus(DocumentStatus.UPLOAD_IN_PROGRESS);
        entity.setTimestampCreated(ownerId.getTimestamp());
        entity.setUsedForVerification(true);
        return documentVerificationRepository.save(entity);
    }

    private SubmittedDocument createSubmittedDocument(
            OwnerId ownerId,
            DocumentSubmitRequest.DocumentMetadata docMetadata,
            List<Document> docs) throws DocumentSubmitException {
        Image photo = new Image();
        photo.setFilename(docMetadata.getFilename());

        SubmittedDocument submittedDoc = new SubmittedDocument();
        submittedDoc.setDocumentId(docMetadata.getUploadId());
        submittedDoc.setPhoto(photo);
        submittedDoc.setSide(docMetadata.getSide());
        submittedDoc.setType(docMetadata.getType());

        if (docMetadata.getUploadId() == null) {
            Optional<Document> docOptional = docs.stream()
                    .filter(doc -> doc.getFilename().equals(docMetadata.getFilename()))
                    .findFirst();
            if (docOptional.isPresent()) {
                photo.setData(docOptional.get().getData());
            } else {
                logger.error("Missing {} in data, {}", docMetadata, ownerId);
                throw new DocumentSubmitException("Not found document data in sent request");
            }
        } else {
            Optional<DocumentDataEntity> docDataOptional =
                    documentDataRepository.findById(docMetadata.getUploadId());
            if (docDataOptional.isPresent()) {
                DocumentDataEntity documentData = docDataOptional.get();
                if (!ownerId.getActivationId().equals(documentData.getActivationId())) {
                    logger.error("The referenced document data uploadId={} are from different activation, {}",
                            docMetadata, ownerId);
                    throw new DocumentSubmitException("Invalid reference on uploaded document data in sent request");
                }
                photo.setData(documentData.getData());
            } else {
                logger.error("Missing {} in data, {}", docMetadata, ownerId);
                throw new DocumentSubmitException("Not found document data in saved upload");
            }
        }
        return submittedDoc;
    }

    private List<Document> getDocuments(OwnerId ownerId, DocumentSubmitRequest request) {
        List<Document> documents;
        if (request.getData() == null) {
            documents = Collections.emptyList();
        } else {
            try {
                documents = dataExtractionService.extractDocuments(request.getData());
            } catch (DocumentVerificationException e) {
                logger.error("Unable to extract documents from {}, {}", request, ownerId);
                documents = Collections.emptyList();
            }
        }
        return documents;
    }

    private void processDocsSubmitResults(OwnerId ownerId, DocumentVerificationEntity docVerification,
                                          DocumentsSubmitResult docsSubmitResults, DocumentSubmitResult docSubmitResult) {
        if (docSubmitResult.getErrorDetail() != null) {
            docVerification.setStatus(DocumentStatus.FAILED);
            docVerification.setErrorDetail(docSubmitResult.getErrorDetail());
        } else if (docSubmitResult.getRejectReason() != null) {
            docVerification.setStatus(DocumentStatus.REJECTED);
            docVerification.setRejectReason(docSubmitResult.getRejectReason());
        } else {
            docVerification.setPhotoId(docsSubmitResults.getExtractedPhotoId());
            docVerification.setProviderName(identityVerificationConfig.getDocumentVerificationProvider());
            if (docVerification.getTimestampUploaded() == null) {
                docVerification.setTimestampUploaded(ownerId.getTimestamp());
            }
            docVerification.setUploadId(docSubmitResult.getUploadId());

            if (DocumentType.SELFIE_PHOTO.equals(docVerification.getType())) {
                docVerification.setStatus(
                        identityVerificationConfig.isVerifySelfieWithDocumentsEnabled() ?
                                DocumentStatus.VERIFICATION_PENDING :
                                DocumentStatus.ACCEPTED
                );
            } else if (docSubmitResult.getExtractedData() == null) { // only finished upload contains extracted data
                docVerification.setStatus(DocumentStatus.UPLOAD_IN_PROGRESS);
            } else if (identityVerificationConfig.isDocumentVerificationOnSubmitEnabled()) {
                verifyDocumentWithUpload(ownerId, docVerification, docSubmitResult.getUploadId());
                docVerification.setStatus(DocumentStatus.UPLOAD_IN_PROGRESS);
            } else { // no document verification during upload, wait for the final all documents verification
                docVerification.setStatus(DocumentStatus.VERIFICATION_PENDING);
            }
        }
    }

    private void verifyDocumentWithUpload(OwnerId ownerId, DocumentVerificationEntity docVerification, String uploadId) {
        try {
            DocumentsVerificationResult docsVerificationResult =
                    documentVerificationProvider.verifyDocuments(ownerId, List.of(uploadId));
            docVerification.setVerificationId(docsVerificationResult.getVerificationId());
        } catch (DocumentVerificationException e) {
            logger.warn("Unable to verify document with uploadId: {}, reason: {}, {}", uploadId, e.getMessage(), ownerId);
            docVerification.setStatus(DocumentStatus.FAILED);
            docVerification.setErrorDetail(e.getMessage());
        }
    }

}
