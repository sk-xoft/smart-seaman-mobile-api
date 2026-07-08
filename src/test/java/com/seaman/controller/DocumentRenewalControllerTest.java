package com.seaman.controller;

import com.seaman.constant.AppStatus;
import com.seaman.model.common.SuccessResponse;
import com.seaman.model.response.DocumentRequestItemUploadResponse;
import com.seaman.service.DocumentRenewalCreateService;
import com.seaman.service.DocumentRenewalItemFileService;
import com.seaman.service.DocumentRenewalService;
import com.seaman.service.MessageCodeService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class DocumentRenewalControllerTest {
    @Test
    void multipartItemUploadDelegatesToService() {
        DocumentRenewalService renewal = mock(DocumentRenewalService.class);
        MessageCodeService messages = mock(MessageCodeService.class);
        DocumentRenewalCreateService create = mock(DocumentRenewalCreateService.class);
        DocumentRenewalItemFileService files = mock(DocumentRenewalItemFileService.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        MultipartFile file = mock(MultipartFile.class);
        DocumentRequestItemUploadResponse data = new DocumentRequestItemUploadResponse();
        data.setProfileRequestItemId("profile-item-id");
        when(files.upload("260700001", "MRI002", "GENERAL", "MAIN", file)).thenReturn(data);
        when(messages.getMessageDescription(eq(AppStatus.SUCCESS_CODE), any())).thenReturn("Success");
        DocumentRenewalController controller =
                new DocumentRenewalController(renewal, messages, create, files);

        ResponseEntity<SuccessResponse<DocumentRequestItemUploadResponse>> response =
                controller.uploadItemFile(request, "260700001", "MRI002", "GENERAL", "MAIN", file);

        assertEquals("profile-item-id", response.getBody().getData().getProfileRequestItemId());
        verify(files).upload("260700001", "MRI002", "GENERAL", "MAIN", file);
    }
}
