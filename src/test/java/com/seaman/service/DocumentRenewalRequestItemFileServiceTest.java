package com.seaman.service;

import com.amazonaws.services.s3.AmazonS3;
import com.seaman.entity.DocumentRequestItemFileEntity;
import com.seaman.exception.BusinessException;
import com.seaman.model.response.DocumentRequestItemFileResponse;
import com.seaman.model.response.DocumentRequestItemUploadResponse;
import com.seaman.repository.DocumentRenewalRequestItemFileRepository;
import com.seaman.repository.DocumentRequestItemFileRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentRenewalRequestItemFileServiceTest {

    // Real byte content recognized by jmimemagic (net.sf.jmimemagic.Magic) used by the service.
    private static final byte[] PDF_BYTES = ("%PDF-1.4\n%test\n1 0 obj\n<< /Type /Catalog >>\nendobj\n"
            + "trailer\n<< /Root 1 0 R >>\n%%EOF").getBytes();
    private static final byte[] PNG_BYTES = new byte[]{
            (byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n',
            0, 0, 0, 13, 'I', 'H', 'D', 'R', 0, 0, 0, 1, 0, 0, 0, 1, 8, 6, 0, 0, 0,
            0x1f, 0x15, (byte) 0xc4, (byte) 0x89};
    private static final byte[] TEXT_BYTES = "hello world this is plain text content for testing".getBytes();

    @Mock DocumentRenewalRequestItemFileRepository repository;
    @Mock DocumentRequestItemFileRepository masterRepository;
    @Mock AmazonS3 s3;

    private DocumentRenewalRequestItemFileService service;

    @BeforeEach
    void setUp() {
        service = new DocumentRenewalRequestItemFileService(repository, masterRepository, s3);
        ReflectionTestUtils.setField(service, "bucketName", "smart-seaman-bucket");
        ReflectionTestUtils.setField(service, "pathTemplate", "document-renewal/request-items/%s/%s");
        TransactionSynchronizationManager.initSynchronization();
    }

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    // ---- normalize (private, exercised via reflection)

    @Test
    void normalizeRejectsNullBlankTooLongOrNonAlphanumeric() {
        assertThrows(BusinessException.class, () -> invokeNormalize(null));
        assertThrows(BusinessException.class, () -> invokeNormalize("   "));
        assertThrows(BusinessException.class, () -> invokeNormalize("A".repeat(21)));
        assertThrows(BusinessException.class, () -> invokeNormalize("BAD-VALUE!"));
    }

    @Test
    void normalizeUppercasesAndTrimsValidInput() throws Exception {
        assertEquals("GENERAL", invokeNormalize("  general  "));
        assertEquals("ID_CARD", invokeNormalize("id_card"));
    }

    // ---- validateCombination (private, exercised via reflection)

    @Test
    void validateCombinationAcceptsMri001IdCardSlots() throws Exception {
        when(masterRepository.isActiveItem("MRI001")).thenReturn(true);
        invokeValidateCombination("MRI001", "ID_CARD", "FRONT");
        invokeValidateCombination("MRI001", "ID_CARD", "BACK");
        invokeValidateCombination("MRI001", "ID_CARD", "MAIN");
    }

    @Test
    void validateCombinationAcceptsMri001PassportMain() throws Exception {
        when(masterRepository.isActiveItem("MRI001")).thenReturn(true);
        invokeValidateCombination("MRI001", "PASSPORT", "MAIN");
    }

    @Test
    void validateCombinationRejectsMri001WithInvalidSlot() {
        assertThrows(BusinessException.class,
                () -> invokeValidateCombination("MRI001", "ID_CARD", "OTHER"));
        assertThrows(BusinessException.class,
                () -> invokeValidateCombination("MRI001", "PASSPORT", "FRONT"));
    }

    @Test
    void validateCombinationAcceptsGeneralItemWithGeneralMain() throws Exception {
        when(masterRepository.isActiveItem("DOC001")).thenReturn(true);
        invokeValidateCombination("DOC001", "GENERAL", "MAIN");
    }

    @Test
    void validateCombinationRejectsNonGeneralItemCombination() {
        assertThrows(BusinessException.class,
                () -> invokeValidateCombination("DOC001", "ID_CARD", "MAIN"));
        assertThrows(BusinessException.class,
                () -> invokeValidateCombination("DOC001", "GENERAL", "FRONT"));
    }

    @Test
    void validateCombinationThrowsWhenItemNotActive() {
        when(masterRepository.isActiveItem("DOC001")).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> invokeValidateCombination("DOC001", "GENERAL", "MAIN"));
        assertEquals("documentRequestItemSlot", ex.getMessage());
    }

    // ---- validateFile (private, exercised via reflection)

    @Test
    void validateFileRejectsNullOrEmptyFile() {
        assertThrows(BusinessException.class, () -> invokeValidateFile(null));
        assertThrows(BusinessException.class,
                () -> invokeValidateFile(new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0])));
    }

    @Test
    void validateFileRejectsDisallowedMimeType() {
        assertThrows(BusinessException.class,
                () -> invokeValidateFile(new MockMultipartFile("file", "note.txt", "text/plain", TEXT_BYTES)));
    }

    @Test
    void validateFileAcceptsAllowedMimeTypes() throws Exception {
        byte[] result = invokeValidateFile(new MockMultipartFile("file", "doc.pdf", "application/pdf", PDF_BYTES));
        assertArrayEquals(PDF_BYTES, result);

        byte[] pngResult = invokeValidateFile(new MockMultipartFile("file", "img.png", "image/png", PNG_BYTES));
        assertArrayEquals(PNG_BYTES, pngResult);
    }

    // ---- detectMime (private, exercised via reflection)

    @Test
    void detectMimeReturnsLowercasedMimeTypeForKnownContent() throws Exception {
        assertEquals("application/pdf", invokeDetectMime(PDF_BYTES));
        assertEquals("image/png", invokeDetectMime(PNG_BYTES));
    }

    @Test
    void detectMimeWrapsFailureAsBusinessException() {
        BusinessException ex = assertThrows(BusinessException.class, () -> invokeDetectMime(new byte[0]));
        assertEquals("fileMimeType", ex.getMessage());
    }

    // ---- safeOriginalName (private, exercised via reflection)

    @Test
    void safeOriginalNameDefaultsWhenNullOrBlank() throws Exception {
        assertEquals("file", invokeSafeOriginalName(null));
        assertEquals("file", invokeSafeOriginalName("   "));
    }

    @Test
    void safeOriginalNameSanitizesSlashesAndTruncates() throws Exception {
        assertEquals("a_b_c.pdf", invokeSafeOriginalName("a/b\\c.pdf"));
        String longName = "a".repeat(300) + ".pdf";
        assertEquals(255, invokeSafeOriginalName(longName).length());
    }

    // ---- mapFiles (public)

    @Test
    void mapFilesMapsFieldsAndAppliesConditionalBranches() {
        DocumentRequestItemFileEntity withUpload = new DocumentRequestItemFileEntity();
        withUpload.setId("f1");
        withUpload.setDocumentType("GENERAL");
        withUpload.setSlotCode("MAIN");
        withUpload.setStorageKey("key1");
        withUpload.setOriginalFileName("doc.pdf");
        withUpload.setMimeType("application/pdf");
        withUpload.setFileSize(1234L);
        withUpload.setFileUploadedAt(new Date());
        withUpload.setCheckResult("pass");
        withUpload.setCheckNote("ok");
        withUpload.setIsUpdated(true);

        DocumentRequestItemFileEntity bare = new DocumentRequestItemFileEntity();
        bare.setId("f2");
        bare.setDocumentType("GENERAL");
        bare.setSlotCode("MAIN");
        bare.setFileUploadedAt(null);
        bare.setCheckResult("");
        bare.setCheckNote(null);
        bare.setIsUpdated(false);

        List<DocumentRequestItemFileResponse> result = service.mapFiles(List.of(withUpload, bare));

        assertEquals(2, result.size());
        DocumentRequestItemFileResponse first = result.get(0);
        assertNotNull(first.getFileUploadedAt());
        assertEquals("pass", first.getCheckResult());
        assertEquals("ok", first.getCheckNote());
        assertTrue(first.getIsUpdated());

        DocumentRequestItemFileResponse second = result.get(1);
        assertNull(second.getFileUploadedAt());
        assertNull(second.getCheckResult());
        assertNull(second.getCheckNote());
        assertNull(second.getIsUpdated());
    }

    @Test
    void mapFilesReturnsEmptyListForEmptyInput() {
        assertTrue(service.mapFiles(Collections.emptyList()).isEmpty());
    }

    // ---- upload (public, end-to-end happy path + obsolete-key cleanup)

    @Test
    void uploadStoresFileAndReturnsCompleteResponse() {
        when(masterRepository.isActiveItem("DOC001")).thenReturn(true);
        when(repository.findFiles("REQ1")).thenReturn(Collections.emptyList());
        when(repository.upsertFile(eq("REQ1"), eq("DOC001"), eq("GENERAL"), eq("MAIN"),
                anyString(), eq("doc.pdf"), eq("application/pdf"), eq((long) PDF_BYTES.length)))
                .thenReturn("file-id-1");
        when(repository.isComplete("REQ1", "DOC001", "GENERAL")).thenReturn(true);

        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", PDF_BYTES);
        DocumentRequestItemUploadResponse response =
                service.upload("REQ1", "doc001", "general", "main", file);

        assertEquals("file-id-1", response.getRequestItemFileId());
        assertEquals("DOC001", response.getItemCode());
        assertEquals("GENERAL", response.getDocumentType());
        assertEquals("REQUEST", response.getStorageScope());
        assertTrue(response.getComplete());
        verify(s3).putObject(eq("smart-seaman-bucket"), anyString(), any(), any());
        verify(repository).deleteOtherTypes("REQ1", "GENERAL");
    }

    @Test
    void uploadMarksObsoleteFilesAndCleansThemUpAfterCommit() {
        when(masterRepository.isActiveItem("DOC001")).thenReturn(true);

        DocumentRequestItemFileEntity sameSlotDifferentType = new DocumentRequestItemFileEntity();
        sameSlotDifferentType.setDocumentType("OTHER");
        sameSlotDifferentType.setSlotCode("MAIN");
        sameSlotDifferentType.setStorageKey("obsolete-key-1");

        DocumentRequestItemFileEntity sameTypeSameSlot = new DocumentRequestItemFileEntity();
        sameTypeSameSlot.setDocumentType("GENERAL");
        sameTypeSameSlot.setSlotCode("MAIN");
        sameTypeSameSlot.setStorageKey("obsolete-key-2");

        when(repository.findFiles("REQ1")).thenReturn(List.of(sameSlotDifferentType, sameTypeSameSlot));
        when(repository.upsertFile(anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyLong())).thenReturn("file-id-1");
        when(repository.isComplete(anyString(), anyString(), anyString())).thenReturn(false);

        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", PDF_BYTES);
        service.upload("REQ1", "doc001", "general", "main", file);

        List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
        assertEquals(1, synchronizations.size());
        synchronizations.get(0).afterCommit();

        verify(s3).deleteObject(eq("smart-seaman-bucket"), eq("obsolete-key-1"));
        verify(s3).deleteObject(eq("smart-seaman-bucket"), eq("obsolete-key-2"));
    }

    @Test
    void uploadRollbackDeletesNewlyUploadedKey() {
        when(masterRepository.isActiveItem("DOC001")).thenReturn(true);
        when(repository.findFiles("REQ1")).thenReturn(Collections.emptyList());
        when(repository.upsertFile(anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyLong())).thenReturn("file-id-1");
        when(repository.isComplete(anyString(), anyString(), anyString())).thenReturn(false);

        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", PDF_BYTES);
        service.upload("REQ1", "doc001", "general", "main", file);

        List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
        synchronizations.get(0).afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);

        verify(s3).deleteObject(eq("smart-seaman-bucket"), anyString());
    }

    @Test
    void uploadThrowsWhenCombinationInvalid() {
        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", PDF_BYTES);

        assertThrows(BusinessException.class,
                () -> service.upload("REQ1", "doc001", "id_card", "main", file));
        verifyNoInteractions(s3);
    }

    // ---- reflection helpers ----

    private String invokeNormalize(String value) throws Exception {
        Method m = DocumentRenewalRequestItemFileService.class.getDeclaredMethod("normalize", String.class, String.class);
        m.setAccessible(true);
        try {
            return (String) m.invoke(service, value, "field");
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw (Exception) e.getCause();
        }
    }

    private void invokeValidateCombination(String itemCode, String documentType, String slotCode) throws Exception {
        Method m = DocumentRenewalRequestItemFileService.class.getDeclaredMethod("validateCombination",
                String.class, String.class, String.class);
        m.setAccessible(true);
        try {
            m.invoke(service, itemCode, documentType, slotCode);
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw (Exception) e.getCause();
        }
    }

    private byte[] invokeValidateFile(Object file) throws Exception {
        Method m = DocumentRenewalRequestItemFileService.class.getDeclaredMethod("validateFile",
                org.springframework.web.multipart.MultipartFile.class);
        m.setAccessible(true);
        try {
            return (byte[]) m.invoke(service, file);
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw (Exception) e.getCause();
        }
    }

    private String invokeDetectMime(byte[] content) throws Exception {
        Method m = DocumentRenewalRequestItemFileService.class.getDeclaredMethod("detectMime", byte[].class);
        m.setAccessible(true);
        try {
            return (String) m.invoke(service, (Object) content);
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw (Exception) e.getCause();
        }
    }

    private String invokeSafeOriginalName(String name) throws Exception {
        Method m = DocumentRenewalRequestItemFileService.class.getDeclaredMethod("safeOriginalName", String.class);
        m.setAccessible(true);
        try {
            return (String) m.invoke(service, name);
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw (Exception) e.getCause();
        }
    }
}
