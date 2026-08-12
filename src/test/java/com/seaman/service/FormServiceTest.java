package com.seaman.service;

import com.amazonaws.services.s3.AmazonS3;
import com.seaman.entity.FormEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.model.response.FormResponse;
import com.seaman.repository.FormRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FormServiceTest {

    @Mock FormRepository formRepository;
    @Mock TransactionLogsService transactionLogsService;
    @Mock AmazonS3 getS3;
    @Mock HttpServletRequest httpServletRequest;

    private FormService service;
    private UsersEntity user;

    @BeforeEach
    void setUp() {
        service = new FormService(formRepository, transactionLogsService, getS3);
        ReflectionTestUtils.setField(service, "bucketName", "smart-seaman-bucket");
        ReflectionTestUtils.setField(service, "downloadFile", "documents/downloads");

        user = new UsersEntity();
        user.setUsername("crew@example.com");
        lenient().when(httpServletRequest.getAttribute(anyString()))
                .thenAnswer(invocation -> "userObject".equals(invocation.getArgument(0)) ? user : null);
    }

    // ---- formAll

    @Test
    void formAllReturnsFormsFromRepository() {
        FormEntity entity = new FormEntity();
        entity.setFormId("F001");
        entity.setFormFileName("form.pdf");
        when(formRepository.findAll()).thenReturn(List.of(entity));

        FormResponse response = service.formAll(httpServletRequest);

        assertEquals(1, response.getForms().size());
        assertEquals("F001", response.getForms().get(0).getFormId());
    }

    @Test
    void formAllReturnsEmptyListWhenNoneFound() {
        when(formRepository.findAll()).thenReturn(Collections.emptyList());

        FormResponse response = service.formAll(httpServletRequest);

        assertTrue(response.getForms().isEmpty());
    }

    // ---- downloadForm

    @Test
    void downloadFormDecodesBase64ContentFromS3() {
        FormEntity entity = new FormEntity();
        entity.setFormId("F001");
        entity.setFormFileName("form.pdf");
        when(formRepository.findById("F001")).thenReturn(entity);
        String encoded = Base64.getEncoder().encodeToString("file-content".getBytes());
        when(getS3.getObjectAsString("smart-seaman-bucket", "documents/downloads/form.pdf"))
                .thenReturn(encoded);

        byte[] result = service.downloadForm(httpServletRequest, "F001");

        assertArrayEquals("file-content".getBytes(), result);
    }

    @Test
    void downloadFormThrowsWhenEntityNotFound() {
        // No null-guard here: missing entity results in an NPE, wrapped by the outer
        // catch(Exception) which sets statusCode and rethrows.
        when(formRepository.findById("missing")).thenReturn(null);

        assertThrows(NullPointerException.class, () -> service.downloadForm(httpServletRequest, "missing"));
    }

    @Test
    void downloadFormThrowsOnMalformedBase64FromS3() {
        FormEntity entity = new FormEntity();
        entity.setFormId("F001");
        entity.setFormFileName("form.pdf");
        when(formRepository.findById("F001")).thenReturn(entity);
        when(getS3.getObjectAsString(anyString(), anyString())).thenReturn("not-valid-base64!!");

        assertThrows(IllegalArgumentException.class, () -> service.downloadForm(httpServletRequest, "F001"));
    }
}
