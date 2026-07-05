package com.seaman.controller;

import com.seaman.constant.AppStatus;
import com.seaman.model.response.DocumentRequestItemUploadResponse;
import com.seaman.service.DocumentRequestItemFileService;
import com.seaman.service.DocumentService;
import com.seaman.service.MessageCodeService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DocumentRequestItemFileControllerTest {
    @Test
    void multipartUploadDelegatesToFileService() {
        MessageCodeService messages = mock(MessageCodeService.class);
        DocumentService documents = mock(DocumentService.class);
        DocumentRequestItemFileService files = mock(DocumentRequestItemFileService.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        MultipartFile file = mock(MultipartFile.class);
        DocumentRequestItemUploadResponse data = new DocumentRequestItemUploadResponse();
        data.setProfileRequestItemId("profile-id");
        when(files.upload("MRI001", "ID_CARD", "FRONT", file)).thenReturn(data);
        when(messages.getMessageDescription(eq(AppStatus.SUCCESS_CODE), any())).thenReturn("Success");
        DocumentController controller = new DocumentController(messages, documents, files);

        ResponseEntity<com.seaman.model.common.SuccessResponse<DocumentRequestItemUploadResponse>> response =
                controller.uploadRequestItemFile(request, "MRI001", "ID_CARD", "FRONT", file);

        assertEquals("profile-id", response.getBody().getData().getProfileRequestItemId());
        verify(files).upload("MRI001", "ID_CARD", "FRONT", file);
    }
}
