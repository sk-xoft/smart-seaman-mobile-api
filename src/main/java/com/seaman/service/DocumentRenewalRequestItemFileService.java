package com.seaman.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.seaman.constant.AppStatus;
import com.seaman.entity.DocumentRequestItemFileEntity;
import com.seaman.exception.BusinessException;
import com.seaman.model.response.DocumentRequestItemFileResponse;
import com.seaman.model.response.DocumentRequestItemUploadResponse;
import com.seaman.repository.DocumentRenewalRequestItemFileRepository;
import com.seaman.repository.DocumentRequestItemFileRepository;
import lombok.RequiredArgsConstructor;
import net.sf.jmimemagic.Magic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentRenewalRequestItemFileService {
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_MIME =
            Set.of("image/jpeg", "image/png", "application/pdf");
    private static final DateTimeFormatter DISPLAY_DATE =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final DocumentRenewalRequestItemFileRepository repository;
    private final DocumentRequestItemFileRepository masterRepository;
    private final AmazonS3 s3;

    @Value("${object.store.bucket}") private String bucketName;
    @Value("${object.store.path.request.items}") private String pathTemplate;

    @Transactional
    public DocumentRequestItemUploadResponse upload(String requestItemId, String itemCodeInput,
                                                     String documentTypeInput, String slotCodeInput,
                                                     MultipartFile file) {
        String itemCode = normalize(itemCodeInput, "itemCode");
        String documentType = normalize(documentTypeInput, "documentType");
        String slotCode = normalize(slotCodeInput, "slotCode");
        validateCombination(itemCode, documentType, slotCode);
        byte[] content = validateFile(file);
        String key = String.format(pathTemplate, requestItemId, UUID.randomUUID());
        String mimeType = detectMime(content);
        uploadObject(key, content, mimeType);

        List<String> obsoleteKeys = new ArrayList<>();
        registerStorageCleanup(key, obsoleteKeys);
        for (DocumentRequestItemFileEntity old : repository.findFiles(requestItemId)) {
            if (!documentType.equals(old.getDocumentType()) || slotCode.equals(old.getSlotCode())) {
                obsoleteKeys.add(old.getStorageKey());
            }
        }
        repository.deleteOtherTypes(requestItemId, documentType);
        String fileId = repository.upsertFile(requestItemId, itemCode, documentType, slotCode, key,
                safeOriginalName(file.getOriginalFilename()), mimeType, content.length);
        boolean complete = repository.isComplete(requestItemId, itemCode, documentType);

        DocumentRequestItemUploadResponse response = new DocumentRequestItemUploadResponse();
        response.setRequestItemFileId(fileId);
        response.setItemCode(itemCode);
        response.setStorageScope("REQUEST");
        response.setDocumentType(documentType);
        response.setComplete(complete);
        response.setFiles(mapFiles(repository.findFiles(requestItemId)));
        return response;
    }

    public List<DocumentRequestItemFileResponse> mapFiles(List<DocumentRequestItemFileEntity> entities) {
        List<DocumentRequestItemFileResponse> result = new ArrayList<>();
        for (DocumentRequestItemFileEntity entity : entities) {
            DocumentRequestItemFileResponse response = new DocumentRequestItemFileResponse();
            response.setFileId(entity.getId());
            response.setDocumentType(entity.getDocumentType());
            response.setSlotCode(entity.getSlotCode());
            response.setOriginalFileName(entity.getOriginalFileName());
            response.setMimeType(entity.getMimeType());
            response.setFileSize(entity.getFileSize());
            if (entity.getFileUploadedAt() != null) {
                response.setFileUploadedAt(entity.getFileUploadedAt().toInstant()
                        .atZone(ZoneId.of("Asia/Bangkok")).format(DISPLAY_DATE));
            }
            response.setCheckResult(entity.getCheckResult());
            response.setCheckNote(entity.getCheckNote());
            response.setIsUpdated(entity.getIsUpdated());
            result.add(response);
        }
        return result;
    }

    private void validateCombination(String itemCode, String documentType, String slotCode) {
        if ("MRI001".equals(itemCode)) {
            boolean valid = ("ID_CARD".equals(documentType)
                    && ("FRONT".equals(slotCode) || "BACK".equals(slotCode)))
                    || ("PASSPORT".equals(documentType) && "MAIN".equals(slotCode));
            if (!valid) throw new BusinessException(AppStatus.INVALID_FORMAT, "documentType/slotCode");
        } else if (!"GENERAL".equals(documentType) || !"MAIN".equals(slotCode)) {
            throw new BusinessException(AppStatus.INVALID_FORMAT, "documentType/slotCode");
        }
        if (!masterRepository.isActiveItem(itemCode)) {
            throw new BusinessException(AppStatus.DATA_NOT_FOUND, "documentRequestItemSlot");
        }
    }

    private byte[] validateFile(MultipartFile file) {
        try {
            if (file == null || file.isEmpty() || file.getSize() <= 0 || file.getSize() > MAX_FILE_SIZE) {
                throw new BusinessException(AppStatus.INVALID_FORMAT, "file");
            }
            byte[] bytes = file.getBytes();
            if (!ALLOWED_MIME.contains(detectMime(bytes))) {
                throw new BusinessException(AppStatus.INVALID_FORMAT, "fileMimeType");
            }
            return bytes;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(AppStatus.INVALID_FORMAT, "file");
        }
    }

    private String detectMime(byte[] content) {
        try {
            return Magic.getMagicMatch(content).getMimeType().toLowerCase(Locale.ROOT);
        } catch (Exception ex) {
            throw new BusinessException(AppStatus.INVALID_FORMAT, "fileMimeType");
        }
    }

    private void uploadObject(String key, byte[] content, String mimeType) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(content.length);
        metadata.setContentType(mimeType);
        s3.putObject(bucketName, key, new ByteArrayInputStream(content), metadata);
    }

    private void registerStorageCleanup(String newKey, List<String> obsoleteKeys) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() {
                obsoleteKeys.stream().filter(k -> k != null && !k.equals(newKey))
                        .forEach(k -> safeDelete(k));
            }
            @Override public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) safeDelete(newKey);
            }
        });
    }

    private void safeDelete(String key) {
        try { s3.deleteObject(bucketName, key); } catch (Exception ignored) { }
    }

    private String normalize(String value, String field) {
        if (value == null || value.trim().isEmpty() || value.trim().length() > 20
                || !value.trim().matches("[A-Za-z0-9_]+")) {
            throw new BusinessException(AppStatus.INVALID_FORMAT, field);
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String safeOriginalName(String name) {
        if (name == null || name.trim().isEmpty()) return "file";
        String cleaned = name.replace("\\", "_").replace("/", "_").trim();
        return cleaned.length() > 255 ? cleaned.substring(0, 255) : cleaned;
    }
}
