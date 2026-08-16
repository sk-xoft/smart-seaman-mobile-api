package com.seaman.service;

import com.amazonaws.services.s3.AmazonS3;
import com.seaman.entity.UsersEntity;
import com.seaman.exception.BusinessException;
import com.seaman.repository.DocumentRequestItemFileRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentRequestItemFileServiceTest {
    private static final byte[] PNG = Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=");
    @Mock DocumentRequestItemFileRepository repository; @Mock AmazonS3 s3;
    @Mock HttpServletRequest request; @Mock MultipartFile file;
    DocumentRequestItemFileService service;
    @BeforeEach void setup() throws Exception {
        service = new DocumentRequestItemFileService(repository, s3, request);
        ReflectionTestUtils.setField(service,"bucketName","bucket");
        ReflectionTestUtils.setField(service,"pathTemplate","documents/%s/request-items/%s");
        UsersEntity user=new UsersEntity(); user.setMobileUuid("user-uuid");
        lenient().when(request.getAttribute("userObject")).thenReturn(user);
        lenient().when(file.isEmpty()).thenReturn(false); lenient().when(file.getSize()).thenReturn((long)PNG.length);
        lenient().when(file.getBytes()).thenReturn(PNG); lenient().when(file.getOriginalFilename()).thenReturn("id.png");
        TransactionSynchronizationManager.initSynchronization();
    }
    @AfterEach void cleanup(){if(TransactionSynchronizationManager.isSynchronizationActive())TransactionSynchronizationManager.clearSynchronization();}
    @Test void idCardFrontRemainsIncomplete(){
        when(repository.isActiveItem("MRI001")).thenReturn(true);
        when(repository.findFiles("user-uuid","MRI001")).thenReturn(Collections.emptyList());
        when(repository.upsertFile(anyString(),anyString(),anyString(),anyString(),anyString(),anyString(),anyString(),anyLong())).thenReturn("file-id");
        when(repository.isComplete("user-uuid","MRI001","ID_CARD")).thenReturn(false);
        assertFalse(service.upload("MRI001","ID_CARD","FRONT",file).getComplete());
    }
    @Test void idCardMainCanComplete(){
        when(repository.isActiveItem("MRI001")).thenReturn(true);
        when(repository.findFiles("user-uuid","MRI001")).thenReturn(Collections.emptyList());
        when(repository.upsertFile(anyString(),anyString(),anyString(),anyString(),anyString(),anyString(),anyString(),anyLong())).thenReturn("file-id");
        when(repository.isComplete("user-uuid","MRI001","ID_CARD")).thenReturn(true);
        assertTrue(service.upload("MRI001","ID_CARD","MAIN",file).getComplete());
    }
    @Test void passportCanComplete(){
        when(repository.isActiveItem("MRI001")).thenReturn(true);
        when(repository.findFiles("user-uuid","MRI001")).thenReturn(Collections.emptyList());
        when(repository.upsertFile(anyString(),anyString(),anyString(),anyString(),anyString(),anyString(),anyString(),anyLong())).thenReturn("file-id");
        when(repository.isComplete("user-uuid","MRI001","PASSPORT")).thenReturn(true);
        assertTrue(service.upload("MRI001","PASSPORT","MAIN",file).getComplete());
    }
    @Test void rejectsInvalidCombination(){assertThrows(BusinessException.class,()->service.upload("MRI001","PASSPORT","FRONT",file));}
    @Test void rejectsOversize(){when(repository.isActiveItem("MRI001")).thenReturn(true);when(file.getSize()).thenReturn(10485761L);assertThrows(BusinessException.class,()->service.upload("MRI001","PASSPORT","MAIN",file));}

    @Test void previewReturnsSignedUrlForUploadedFile() throws Exception {
        when(repository.isActiveItem("MRI001")).thenReturn(true);
        com.seaman.entity.DocumentRequestItemFileEntity uploaded = new com.seaman.entity.DocumentRequestItemFileEntity();
        uploaded.setId("file-id");
        uploaded.setDocumentType("ID_CARD");
        uploaded.setSlotCode("MAIN");
        uploaded.setStorageKey("documents/user-uuid/request-items/key.png");
        uploaded.setFileUploaded(1);
        when(repository.findFiles("user-uuid","MRI001")).thenReturn(Collections.singletonList(uploaded));
        when(s3.generatePresignedUrl(eq("bucket"), eq("documents/user-uuid/request-items/key.png"), any()))
                .thenReturn(new java.net.URL("https://example.com/signed"));

        com.seaman.model.response.DocumentRequestItemPreviewResponse response = service.preview("MRI001");

        assertEquals("MRI001", response.getDocumentMasterRequestItemCode());
        assertEquals("ID_CARD", response.getDocumentType());
        assertEquals(1, response.getFiles().size());
        assertEquals("https://example.com/signed", response.getFiles().get(0).getFileUrl());
    }

    @Test void previewReturnsEmptyFilesWhenNothingUploaded(){
        when(repository.isActiveItem("MRI001")).thenReturn(true);
        when(repository.findFiles("user-uuid","MRI001")).thenReturn(Collections.emptyList());

        com.seaman.model.response.DocumentRequestItemPreviewResponse response = service.preview("MRI001");

        assertNull(response.getDocumentType());
        assertTrue(response.getFiles().isEmpty());
    }

    @Test void previewRejectsInvalidItemCode(){assertThrows(BusinessException.class,()->service.preview("bad code!"));}

    @Test void previewRejectsInactiveItem(){
        when(repository.isActiveItem("MRI999")).thenReturn(false);
        assertThrows(BusinessException.class,()->service.preview("MRI999"));
    }
}
