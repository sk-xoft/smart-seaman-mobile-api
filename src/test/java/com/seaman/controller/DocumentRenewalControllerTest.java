package com.seaman.controller;

import com.seaman.constant.AppStatus;
import com.seaman.model.common.SuccessResponse;
import com.seaman.model.response.DocumentRequestItemUploadResponse;
import com.seaman.model.response.DocumentRenewalResubmitResponse;
import com.seaman.model.response.PageDocumentRenewalResponse;
import com.seaman.model.response.DocumentRenewalTimelineResponse;
import com.seaman.model.response.DocumentRenewalDetailResponse;
import com.seaman.service.DocumentRenewalCreateService;
import com.seaman.service.DocumentRenewalItemFileService;
import com.seaman.service.DocumentRenewalResubmitService;
import com.seaman.service.DocumentRenewalListService;
import com.seaman.service.DocumentRenewalTimelineService;
import com.seaman.service.DocumentRenewalDetailService;
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
        DocumentRenewalResubmitService resubmit = mock(DocumentRenewalResubmitService.class);
        DocumentRenewalListService list = mock(DocumentRenewalListService.class);
        DocumentRenewalTimelineService timeline = mock(DocumentRenewalTimelineService.class);
        DocumentRenewalDetailService detail = mock(DocumentRenewalDetailService.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        MultipartFile file = mock(MultipartFile.class);
        DocumentRequestItemUploadResponse data = new DocumentRequestItemUploadResponse();
        data.setProfileRequestItemId("profile-item-id");
        when(files.upload("260700001", "MRI002", "GENERAL", "MAIN", file)).thenReturn(data);
        when(messages.getMessageDescription(eq(AppStatus.SUCCESS_CODE), any())).thenReturn("Success");
        DocumentRenewalController controller =
                new DocumentRenewalController(
                        renewal, messages, create, files, resubmit, list, timeline, detail);

        ResponseEntity<SuccessResponse<DocumentRequestItemUploadResponse>> response =
                controller.uploadItemFile(request, "260700001", "MRI002", "GENERAL", "MAIN", file);

        assertEquals("profile-item-id", response.getBody().getData().getProfileRequestItemId());
        verify(files).upload("260700001", "MRI002", "GENERAL", "MAIN", file);
    }

    @Test
    void resubmitDelegatesByRequestNumber() {
        DocumentRenewalService renewal = mock(DocumentRenewalService.class);
        MessageCodeService messages = mock(MessageCodeService.class);
        DocumentRenewalCreateService create = mock(DocumentRenewalCreateService.class);
        DocumentRenewalItemFileService files = mock(DocumentRenewalItemFileService.class);
        DocumentRenewalResubmitService resubmit = mock(DocumentRenewalResubmitService.class);
        DocumentRenewalListService list = mock(DocumentRenewalListService.class);
        DocumentRenewalTimelineService timeline = mock(DocumentRenewalTimelineService.class);
        DocumentRenewalDetailService detail = mock(DocumentRenewalDetailService.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        DocumentRenewalResubmitResponse data = new DocumentRenewalResubmitResponse(
                "260700001", "PENDING_APPLICANT_CORRECTION",
                "PENDING_DOCUMENT_REVIEW", "RESUBMIT");
        when(resubmit.resubmit("260700001")).thenReturn(data);
        when(messages.getMessageDescription(eq(AppStatus.SUCCESS_CODE), any())).thenReturn("Success");
        DocumentRenewalController controller =
                new DocumentRenewalController(
                        renewal, messages, create, files, resubmit, list, timeline, detail);

        ResponseEntity<SuccessResponse<DocumentRenewalResubmitResponse>> response =
                controller.resubmit(request, "260700001");

        assertEquals("260700001", response.getBody().getData().getRequestNo());
        verify(resubmit).resubmit("260700001");
    }

    @Test
    void listMyRequestsDelegatesOffset() {
        DocumentRenewalService renewal = mock(DocumentRenewalService.class);
        MessageCodeService messages = mock(MessageCodeService.class);
        DocumentRenewalCreateService create = mock(DocumentRenewalCreateService.class);
        DocumentRenewalItemFileService files = mock(DocumentRenewalItemFileService.class);
        DocumentRenewalResubmitService resubmit = mock(DocumentRenewalResubmitService.class);
        DocumentRenewalListService list = mock(DocumentRenewalListService.class);
        DocumentRenewalTimelineService timeline = mock(DocumentRenewalTimelineService.class);
        DocumentRenewalDetailService detail = mock(DocumentRenewalDetailService.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        PageDocumentRenewalResponse data = new PageDocumentRenewalResponse();
        data.setItemTotal(12);
        when(list.listMyRequests(10)).thenReturn(data);
        when(messages.getMessageDescription(eq(AppStatus.SUCCESS_CODE), any())).thenReturn("Success");
        DocumentRenewalController controller =
                new DocumentRenewalController(
                        renewal, messages, create, files, resubmit, list, timeline, detail);

        ResponseEntity<SuccessResponse<PageDocumentRenewalResponse>> response =
                controller.myRequests(request, 10);

        assertEquals(12, response.getBody().getData().getItemTotal());
        verify(list).listMyRequests(10);
    }

    @Test
    void timelineDelegatesByRequestNumber() {
        DocumentRenewalService renewal = mock(DocumentRenewalService.class);
        MessageCodeService messages = mock(MessageCodeService.class);
        DocumentRenewalCreateService create = mock(DocumentRenewalCreateService.class);
        DocumentRenewalItemFileService files = mock(DocumentRenewalItemFileService.class);
        DocumentRenewalResubmitService resubmit = mock(DocumentRenewalResubmitService.class);
        DocumentRenewalListService list = mock(DocumentRenewalListService.class);
        DocumentRenewalTimelineService timeline = mock(DocumentRenewalTimelineService.class);
        DocumentRenewalDetailService detail = mock(DocumentRenewalDetailService.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        DocumentRenewalTimelineResponse data = new DocumentRenewalTimelineResponse();
        data.setRequestNo("260700001");
        when(timeline.timeline("260700001")).thenReturn(data);
        when(messages.getMessageDescription(eq(AppStatus.SUCCESS_CODE), any())).thenReturn("Success");
        DocumentRenewalController controller = new DocumentRenewalController(
                renewal, messages, create, files, resubmit, list, timeline, detail);

        ResponseEntity<SuccessResponse<DocumentRenewalTimelineResponse>> response =
                controller.timeline(request, "260700001");

        assertEquals("260700001", response.getBody().getData().getRequestNo());
        verify(timeline).timeline("260700001");
    }

    @Test
    void detailDelegatesByRequestNumber() {
        DocumentRenewalService renewal = mock(DocumentRenewalService.class);
        MessageCodeService messages = mock(MessageCodeService.class);
        DocumentRenewalCreateService create = mock(DocumentRenewalCreateService.class);
        DocumentRenewalItemFileService files = mock(DocumentRenewalItemFileService.class);
        DocumentRenewalResubmitService resubmit = mock(DocumentRenewalResubmitService.class);
        DocumentRenewalListService list = mock(DocumentRenewalListService.class);
        DocumentRenewalTimelineService timeline = mock(DocumentRenewalTimelineService.class);
        DocumentRenewalDetailService detail = mock(DocumentRenewalDetailService.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        DocumentRenewalDetailResponse data = new DocumentRenewalDetailResponse();
        data.setRequestNo("260700001");
        when(detail.detail("260700001")).thenReturn(data);
        when(messages.getMessageDescription(eq(AppStatus.SUCCESS_CODE), any())).thenReturn("Success");
        DocumentRenewalController controller = new DocumentRenewalController(
                renewal, messages, create, files, resubmit, list, timeline, detail);

        ResponseEntity<SuccessResponse<DocumentRenewalDetailResponse>> response =
                controller.detail(request, "260700001");

        assertEquals("260700001", response.getBody().getData().getRequestNo());
        verify(detail).detail("260700001");
    }
}
