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
package com.wultra.app.enrollmentserver.impl.service;

import com.wultra.app.enrollmentserver.api.model.request.DocumentStatusRequest;
import com.wultra.app.enrollmentserver.api.model.request.DocumentSubmitRequest;
import com.wultra.app.enrollmentserver.api.model.response.DocumentStatusResponse;
import com.wultra.app.enrollmentserver.api.model.response.data.DocumentMetadataResponseDto;
import com.wultra.app.enrollmentserver.configuration.IdentityVerificationConfig;
import com.wultra.app.enrollmentserver.database.DocumentDataRepository;
import com.wultra.app.enrollmentserver.database.DocumentVerificationRepository;
import com.wultra.app.enrollmentserver.database.IdentityVerificationRepository;
import com.wultra.app.enrollmentserver.database.entity.DocumentResultEntity;
import com.wultra.app.enrollmentserver.database.entity.DocumentVerificationEntity;
import com.wultra.app.enrollmentserver.database.entity.IdentityVerificationEntity;
import com.wultra.app.enrollmentserver.errorhandling.*;
import com.wultra.app.enrollmentserver.impl.service.document.DocumentProcessingService;
import com.wultra.app.enrollmentserver.impl.service.verification.VerificationProcessingService;
import com.wultra.app.enrollmentserver.model.enumeration.DocumentStatus;
import com.wultra.app.enrollmentserver.model.enumeration.DocumentType;
import com.wultra.app.enrollmentserver.model.enumeration.IdentityVerificationPhase;
import com.wultra.app.enrollmentserver.model.enumeration.IdentityVerificationStatus;
import com.wultra.app.enrollmentserver.model.integration.DocumentsVerificationResult;
import com.wultra.app.enrollmentserver.model.integration.Image;
import com.wultra.app.enrollmentserver.model.integration.OwnerId;
import com.wultra.app.enrollmentserver.model.integration.VerificationSdkInfo;
import com.wultra.app.enrollmentserver.provider.DocumentVerificationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementing document identity verification.
 *
 * @author Roman Strobl, roman.strobl@wultra.com
 * @author Lukas Lukovsky, lukas.lukovsky@wultra.com
 */
@Service
public class IdentityVerificationService {

    private static final Logger logger = LoggerFactory.getLogger(IdentityVerificationService.class);

    private final IdentityVerificationConfig identityVerificationConfig;
    private final DocumentDataRepository documentDataRepository;
    private final DocumentVerificationRepository documentVerificationRepository;
    private final IdentityVerificationRepository identityVerificationRepository;
    private final DocumentProcessingService documentProcessingService;
    private final IdentityVerificationCreateService identityVerificationCreateService;
    private final VerificationProcessingService verificationProcessingService;
    private final DocumentVerificationProvider documentVerificationProvider;
    private final IdentityVerificationResetService identityVerificationResetService;

    private static final List<DocumentStatus> DOCUMENT_STATUSES_PROCESSED = Arrays.asList(DocumentStatus.ACCEPTED, DocumentStatus.FAILED, DocumentStatus.REJECTED);

    /**
     * Service constructor.
     * @param identityVerificationConfig Identity verification config.
     * @param documentDataRepository Document data repository.
     * @param documentVerificationRepository Document verification repository.
     * @param identityVerificationRepository Identity verification repository.
     * @param documentProcessingService Document processing service.
     * @param identityVerificationCreateService Identity verification create service.
     * @param verificationProcessingService Verification processing service.
     * @param documentVerificationProvider Document verification provider.
     * @param identityVerificationResetService Identity verification reset service.
     */
    @Autowired
    public IdentityVerificationService(
            IdentityVerificationConfig identityVerificationConfig,
            DocumentDataRepository documentDataRepository,
            DocumentVerificationRepository documentVerificationRepository,
            IdentityVerificationRepository identityVerificationRepository,
            DocumentProcessingService documentProcessingService,
            IdentityVerificationCreateService identityVerificationCreateService,
            VerificationProcessingService verificationProcessingService,
            DocumentVerificationProvider documentVerificationProvider,
            IdentityVerificationResetService identityVerificationResetService) {
        this.identityVerificationConfig = identityVerificationConfig;
        this.documentDataRepository = documentDataRepository;
        this.documentVerificationRepository = documentVerificationRepository;
        this.identityVerificationRepository = identityVerificationRepository;
        this.documentProcessingService = documentProcessingService;
        this.identityVerificationCreateService = identityVerificationCreateService;
        this.verificationProcessingService = verificationProcessingService;
        this.documentVerificationProvider = documentVerificationProvider;
        this.identityVerificationResetService = identityVerificationResetService;
    }

    /**
     * Finds the current verification identity
     * @param ownerId Owner identification.
     * @return Optional entity of the verification identity
     */
    public Optional<IdentityVerificationEntity> findBy(OwnerId ownerId) {
        return identityVerificationRepository.findFirstByActivationIdOrderByTimestampCreatedDesc(ownerId.getActivationId());
    }

    /**
     * Initialize identity verification.
     * @param ownerId Owner identification.
     * @param processId Process identifier.
     * @throws IdentityVerificationException Thrown when identity verification initialization fails.
     * @throws RemoteCommunicationException Thrown when communication with PowerAuth server fails.
     * @throws OnboardingProcessException Thrown when onboarding process is invalid.
     */
    public void initializeIdentityVerification(OwnerId ownerId, String processId) throws IdentityVerificationException, RemoteCommunicationException, OnboardingProcessException {
        identityVerificationCreateService.createIdentityVerification(ownerId, processId);
    }

    /**
     * Submit identity-related documents for verification.
     * @param request Document submit request.
     * @param ownerId Owner identification.
     * @return Document verification entities.
     */
    public List<DocumentVerificationEntity> submitDocuments(DocumentSubmitRequest request,
                                                            OwnerId ownerId)
            throws DocumentSubmitException {

        // Find an already existing identity verification
        Optional<IdentityVerificationEntity> idVerificationOptional = findBy(ownerId);

        if (!idVerificationOptional.isPresent()) {
            logger.error("Identity verification has not been initialized, {}", ownerId);
            throw new DocumentSubmitException("Identity verification has not been initialized");
        }

        IdentityVerificationEntity idVerification = idVerificationOptional.get();

        String processId = idVerification.getProcessId();
        if (!processId.equals(request.getProcessId())) {
            logger.warn("Invalid process ID in request: {}", processId);
            throw new DocumentSubmitException("Invalid process ID");
        }

        if (!IdentityVerificationPhase.DOCUMENT_UPLOAD.equals(idVerification.getPhase())) {
            logger.error("The verification phase is {} but expected {}, {}",
                    idVerification.getPhase(), IdentityVerificationPhase.DOCUMENT_UPLOAD, ownerId
            );
            throw new DocumentSubmitException("Not allowed submit of documents during not upload phase");
        } else if (IdentityVerificationStatus.VERIFICATION_PENDING.equals(idVerification.getStatus())) {
            logger.info("Switching {} from {} to {} due to new documents submit, {}",
                    idVerification, IdentityVerificationStatus.VERIFICATION_PENDING, IdentityVerificationStatus.IN_PROGRESS, ownerId
            );
            idVerification.setPhase(IdentityVerificationPhase.DOCUMENT_UPLOAD);
            idVerification.setStatus(IdentityVerificationStatus.IN_PROGRESS);
            idVerification.setTimestampLastUpdated(ownerId.getTimestamp());
            identityVerificationRepository.save(idVerification);
        } else if (!IdentityVerificationStatus.IN_PROGRESS.equals(idVerification.getStatus())) {
            logger.error("The verification status is {} but expected {}, {}",
                    idVerification.getStatus(), IdentityVerificationStatus.IN_PROGRESS, ownerId
            );
            throw new DocumentSubmitException("Not allowed submit of documents during not in progress status");
        }

        List<DocumentVerificationEntity> docsVerifications =
                documentProcessingService.submitDocuments(idVerification, request, ownerId);
        documentProcessingService.pairTwoSidedDocuments(docsVerifications);

        identityVerificationRepository.save(idVerification);
        return docsVerifications;
    }

    /**
     * Starts the verification process
     *
     * @param ownerId Owner identification.
     * @throws IdentityVerificationException Thrown when identity verification could not be started.
     * @throws DocumentVerificationException Thrown when document verification fails.
     */
    @Transactional
    public void startVerification(OwnerId ownerId) throws IdentityVerificationException, DocumentVerificationException {
        Optional<IdentityVerificationEntity> identityVerificationOptional =
                identityVerificationRepository.findFirstByActivationIdOrderByTimestampCreatedDesc(ownerId.getActivationId());

        if (!identityVerificationOptional.isPresent()) {
            logger.error("No identity verification entity found to start the verification, {}", ownerId);
            throw new IdentityVerificationException("Unable to start verification");
        }
        IdentityVerificationEntity identityVerification = identityVerificationOptional.get();

        List<DocumentVerificationEntity> docVerifications =
                documentVerificationRepository.findAllDocumentVerifications(identityVerification,
                        Collections.singletonList(DocumentStatus.VERIFICATION_PENDING));

        List<DocumentVerificationEntity> selfiePhotoVerifications =
                docVerifications.stream()
                        .filter(entity -> DocumentType.SELFIE_PHOTO.equals(entity.getType()))
                        .collect(Collectors.toList());

        // If not enabled then remove selfie photos from the verification process
        if (!identityVerificationConfig.isVerifySelfieWithDocumentsEnabled()) {
            docVerifications.removeAll(selfiePhotoVerifications);
        }

        documentProcessingService.pairTwoSidedDocuments(docVerifications);

        List<String> uploadIds = docVerifications.stream()
                .map(DocumentVerificationEntity::getUploadId)
                .collect(Collectors.toList());

        DocumentsVerificationResult result = documentVerificationProvider.verifyDocuments(ownerId, uploadIds);

        identityVerification.setPhase(IdentityVerificationPhase.DOCUMENT_VERIFICATION);
        identityVerification.setStatus(IdentityVerificationStatus.IN_PROGRESS);
        identityVerification.setTimestampLastUpdated(ownerId.getTimestamp());

        docVerifications.forEach(docVerification -> {
            docVerification.setStatus(DocumentStatus.VERIFICATION_IN_PROGRESS);
            docVerification.setVerificationId(result.getVerificationId());
            docVerification.setTimestampLastUpdated(ownerId.getTimestamp());
        });
        documentVerificationRepository.saveAll(docVerifications);

        // If selfie photos are not included in the verification process with documents change their status to ACCEPTED
        if (!identityVerificationConfig.isVerifySelfieWithDocumentsEnabled()) {
            selfiePhotoVerifications.forEach(selfiePhotoVerification -> {
                selfiePhotoVerification.setStatus(DocumentStatus.ACCEPTED);
                selfiePhotoVerification.setTimestampLastUpdated(ownerId.getTimestamp());
            });
            documentVerificationRepository.saveAll(selfiePhotoVerifications);
        }
    }

    /**
     * Checks verification result and evaluates the final state of the identity verification process
     *
     * @param ownerId Owner identification.
     * @param idVerification Verification identity
     * @throws DocumentVerificationException Thrown when an error during verification check occurred.
     */
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void checkVerificationResult(IdentityVerificationPhase phase, OwnerId ownerId, IdentityVerificationEntity idVerification)
            throws DocumentVerificationException {
        List<DocumentVerificationEntity> allDocVerifications =
                documentVerificationRepository.findAllDocumentVerifications(idVerification,
                        Collections.singletonList(DocumentStatus.VERIFICATION_IN_PROGRESS));
        Map<String, List<DocumentVerificationEntity>> verificationsById = new HashMap<>();

        for (DocumentVerificationEntity docVerification : allDocVerifications) {
            verificationsById.computeIfAbsent(docVerification.getVerificationId(), verificationId -> new ArrayList<>())
                    .add(docVerification);
        }

        for (String verificationId : verificationsById.keySet()) {
            DocumentsVerificationResult docVerificationResult =
                    documentVerificationProvider.getVerificationResult(ownerId, verificationId);
            List<DocumentVerificationEntity> docVerifications = verificationsById.get(verificationId);
            verificationProcessingService.processVerificationResult(ownerId, docVerifications, docVerificationResult);
        }

        if (allDocVerifications.stream()
                .anyMatch(docVerification -> DocumentStatus.VERIFICATION_IN_PROGRESS.equals(docVerification.getStatus()))) {
            return;
        }

        resolveIdentityVerificationResult(phase, idVerification, allDocVerifications);
        idVerification.setTimestampLastUpdated(ownerId.getTimestamp());
        identityVerificationRepository.save(idVerification);
    }

    /**
     * Process identity verification result for document verifications which have already been previously processed.
     * @param ownerId Owner identification.
     * @param idVerification Identity verification entity.
     * @param phase Identity verification phase.
     */
    @Transactional
    public void processDocumentVerificationResult(OwnerId ownerId,
                                                  IdentityVerificationEntity idVerification,
                                                  IdentityVerificationPhase phase) {
        List<DocumentVerificationEntity> processedDocVerifications =
                documentVerificationRepository.findAllDocumentVerifications(idVerification, DOCUMENT_STATUSES_PROCESSED);
        resolveIdentityVerificationResult(phase, idVerification, processedDocVerifications);
        idVerification.setTimestampLastUpdated(ownerId.getTimestamp());
        identityVerificationRepository.save(idVerification);
    }

    /**
     * Resolve identity verification result for an identity verification entity using specified document verification results.
     * @param phase Identity verification phase.
     * @param idVerification Identity verification entity.
     * @param docVerifications Document verification results.
     */
    private void resolveIdentityVerificationResult(
            IdentityVerificationPhase phase,
            IdentityVerificationEntity idVerification,
            List<DocumentVerificationEntity> docVerifications) {
        if (docVerifications.stream()
                .allMatch(docVerification -> DocumentStatus.ACCEPTED.equals(docVerification.getStatus()))) {
            idVerification.setPhase(phase);
            idVerification.setStatus(IdentityVerificationStatus.ACCEPTED);
        } else {
            docVerifications.stream()
                    .filter(docVerification -> DocumentStatus.FAILED.equals(docVerification.getStatus()))
                    .findAny()
                    .ifPresent(failed -> {
                        idVerification.setPhase(phase);
                        idVerification.setStatus(IdentityVerificationStatus.FAILED);
                        idVerification.setErrorDetail(failed.getErrorDetail());
                    });

            docVerifications.stream()
                    .filter(docVerification -> DocumentStatus.REJECTED.equals(docVerification.getStatus()))
                    .findAny()
                    .ifPresent(failed -> {
                        idVerification.setPhase(phase);
                        idVerification.setStatus(IdentityVerificationStatus.REJECTED);
                        idVerification.setErrorDetail(failed.getRejectReason());
                    });
        }
    }

    /**
     * Check status of document verification related to identity.
     * @param request Document status request.
     * @param ownerId Owner identification.
     * @return Document status response.
     * @throws IdentityVerificationException Thrown when identity verification fails.
     */
    @Transactional
    public DocumentStatusResponse checkIdentityVerificationStatus(DocumentStatusRequest request, OwnerId ownerId) throws IdentityVerificationException {
        DocumentStatusResponse response = new DocumentStatusResponse();

        Optional<IdentityVerificationEntity> idVerificationOptional =
                identityVerificationRepository.findFirstByActivationIdOrderByTimestampCreatedDesc(ownerId.getActivationId());

        if (!idVerificationOptional.isPresent()) {
            logger.error("Checking identity verification status on a not existing entity, {}", ownerId);
            response.setStatus(IdentityVerificationStatus.FAILED);
            return response;
        }

        final IdentityVerificationEntity idVerification = idVerificationOptional.get();

        final List<DocumentVerificationEntity> entities;
        if (request.getFilter() != null) {
            final List<String> documentIds = request.getFilter().stream()
                    .map(DocumentStatusRequest.DocumentFilter::getDocumentId)
                    .collect(Collectors.toList());
            entities = Streamable.of(documentVerificationRepository.findAllById(documentIds)).toList();
        } else {
            entities = idVerification.getDocumentVerifications().stream()
                    .filter(DocumentVerificationEntity::isUsedForVerification)
                    .collect(Collectors.toList());
        }

        // Ensure that all entities are related to the identity verification
        if (!entities.isEmpty()) {
            for (DocumentVerificationEntity entity : entities) {
                if (!entity.getActivationId().equals(idVerification.getActivationId())) {
                    logger.error("Not related {} to {}, {}", entity, idVerification, ownerId);
                    response.setStatus(IdentityVerificationStatus.FAILED);
                    return response;
                }
            }
        }

        // Check statuses of all documents used for the verification, update identity verification status accordingly
        if (IdentityVerificationPhase.DOCUMENT_UPLOAD.equals(idVerification.getPhase())
                && IdentityVerificationStatus.IN_PROGRESS.equals(idVerification.getStatus())) {
            checkIdentityDocumentsForVerification(ownerId, idVerification);
        }

        List<DocumentMetadataResponseDto> docsMetadata = createDocsMetadata(entities);
        response.setStatus(idVerification.getStatus());
        response.setDocuments(docsMetadata);

        return response;
    }

    /**
     * Cleanup documents related to identity verification.
     * @param ownerId Owner identification.
     * @throws DocumentVerificationException Thrown when document cleanup fails
     * @throws PresenceCheckException Thrown when presence check cleanup fails.
     * @throws RemoteCommunicationException Thrown when communication with PowerAuth server fails.
     */
    @Transactional
    public void cleanup(OwnerId ownerId)
            throws DocumentVerificationException, PresenceCheckException, RemoteCommunicationException {

        List<String> uploadIds = documentVerificationRepository.findAllUploadIds(ownerId.getActivationId());

        if (identityVerificationConfig.isDocumentVerificationCleanupEnabled()) {
            documentVerificationProvider.cleanupDocuments(ownerId, uploadIds);
        } else {
            logger.debug("Skipped cleanup of documents at document verification provider (not enabled), {}", ownerId);
        }

        // Delete all large documents by activation ID
        documentDataRepository.deleteAllByActivationId(ownerId.getActivationId());
        // Set status of all not finished document verifications to failed
        documentVerificationRepository.failVerifications(ownerId.getActivationId(), ownerId.getTimestamp(), DocumentStatus.ALL_NOT_FINISHED);
        // Set status of all currently running identity verifications to failed
        identityVerificationRepository.failRunningVerifications(ownerId.getActivationId(), ownerId.getTimestamp());
        // Reset activation flags, the client is expected to call /api/identity/init for the next round of verification
        identityVerificationResetService.resetIdentityVerification(ownerId);
    }

    /**
     * Provides photo data
     * @param photoId Identification of the photo
     * @return Photo image
     * @throws DocumentVerificationException When an error occurred during
     */
    public Image getPhotoById(String photoId) throws DocumentVerificationException {
        return documentVerificationProvider.getPhoto(photoId);
    }

    /**
     * Check documents used for verification on their status
     * <p>
     *     When all of the documents are {@link DocumentStatus#VERIFICATION_PENDING} the identity verification is set
     *     also to {@link IdentityVerificationStatus#VERIFICATION_PENDING}
     * </p>
     * @param idVerification Identity verification entity.
     * @param ownerId Owner identification
     */
    @Transactional
    public void checkIdentityDocumentsForVerification(OwnerId ownerId, IdentityVerificationEntity idVerification) {
        List<DocumentVerificationEntity> docVerifications =
                documentVerificationRepository.findAllUsedForVerification(idVerification);

        if (docVerifications.stream()
                .allMatch(docVerification ->
                        DocumentStatus.VERIFICATION_PENDING.equals(docVerification.getStatus())
                )
        ) {
            logger.info("All documents are pending verification, changing status of {} to {}",
                    idVerification, IdentityVerificationStatus.VERIFICATION_PENDING
            );
            idVerification.setPhase(IdentityVerificationPhase.DOCUMENT_UPLOAD);
            idVerification.setStatus(IdentityVerificationStatus.VERIFICATION_PENDING);
            idVerification.setTimestampLastUpdated(ownerId.getTimestamp());
        }
    }

    public List<DocumentMetadataResponseDto> createDocsMetadata(List<DocumentVerificationEntity> entities) {
        List<DocumentMetadataResponseDto> docsMetadata = new ArrayList<>();
        entities.forEach(entity -> {
            DocumentMetadataResponseDto docMetadata = toDocumentMetadata(entity);

            if (DocumentStatus.REJECTED.equals(entity.getStatus())) {
                List<String> errors = collectRejectionErrors(entity);
                if (docMetadata.getErrors() == null) {
                    docMetadata.setErrors(new ArrayList<>());
                }
                docMetadata.getErrors().addAll(errors);
            }

            docsMetadata.add(docMetadata);
        });
        return docsMetadata;
    }

    /**
     * Initializes verification SDK.
     * @param ownerId Owner identification.
     * @param initAttributes SDK initialization attributes.
     * @return
     * @throws DocumentVerificationException
     */
    public VerificationSdkInfo initVerificationSdk(OwnerId ownerId, Map<String, String> initAttributes)
            throws DocumentVerificationException {
        VerificationSdkInfo verificationSdkInfo = documentVerificationProvider.initVerificationSdk(ownerId, initAttributes);
        logger.info("Initialized verification SDK, {}", ownerId);
        return verificationSdkInfo;
    }

    private List<String> collectRejectionErrors(DocumentVerificationEntity entity) {
        List<String> errors = new ArrayList<>();

        // Collect all rejection reasons from the latest document result
        Optional<DocumentResultEntity> docResultOptional = entity.getResults().stream().findFirst();
        if (docResultOptional.isPresent()) {
            DocumentResultEntity docResult = docResultOptional.get();
            List<String> rejectionReasons;
            try {
                rejectionReasons = documentVerificationProvider.parseRejectionReasons(docResult);
            } catch (DocumentVerificationException e) {
                logger.debug("Parsing rejection reasons failure", e);
                logger.warn("Unable to parse rejection reasons from {} of a rejected {}", docResult, entity);
                return Collections.emptyList();
            }
            if (rejectionReasons.isEmpty()) {
                logger.warn("No rejection reasons found in {} of a rejected {}", docResult, entity);
            } else {
                errors.addAll(rejectionReasons);
            }
        } else {
            logger.warn("Missing document result for {}, defaulting errors to reject reason", entity);
            errors.add(entity.getRejectReason());
        }
        return errors;
    }

    /**
     * Create {@link DocumentMetadataResponseDto} from {@link DocumentVerificationEntity}
     * @param entity Document verification entity.
     * @return Document metadata for response
     */
    private DocumentMetadataResponseDto toDocumentMetadata(DocumentVerificationEntity entity) {
        DocumentMetadataResponseDto docMetadata = new DocumentMetadataResponseDto();
        docMetadata.setId(entity.getId());
        if (entity.getErrorDetail() != null) {
            docMetadata.setErrors(List.of(entity.getErrorDetail()));
        }
        docMetadata.setFilename(entity.getFilename());
        docMetadata.setSide(entity.getSide());
        docMetadata.setStatus(entity.getStatus());
        docMetadata.setType(entity.getType());
        return docMetadata;
    }

}