package com.seaman.controller;

import com.seaman.constant.AppStatus;
import com.seaman.model.common.SuccessResponse;
import com.seaman.model.request.DocumentRenewalCreateRequest;
import com.seaman.model.request.DocumentRenewalMobileRequest;
import com.seaman.model.request.DocumentRenewalPaymentRequest;
import com.seaman.model.response.DocumentRenewalCreateResponse;
import com.seaman.model.response.DocumentRenewalMobileResponse;
import com.seaman.model.response.DocumentRenewalPriceResponse;
import com.seaman.model.response.DocumentRequestItemUploadResponse;
import com.seaman.model.response.DocumentRenewalResubmitResponse;
import com.seaman.model.response.DocumentRenewalStatusResponse;
import com.seaman.model.response.PageDocumentRenewalResponse;
import com.seaman.model.response.DocumentRenewalTimelineResponse;
import com.seaman.model.response.DocumentRenewalDetailResponse;
import com.seaman.model.response.DocumentRenewalDetailItemResponse;
import com.seaman.model.response.DocumentRenewalPaymentResponse;
import com.seaman.service.DocumentRenewalCreateService;
import com.seaman.service.DocumentRenewalItemFileService;
import com.seaman.service.DocumentRenewalResubmitService;
import com.seaman.service.DocumentRenewalListService;
import com.seaman.service.DocumentRenewalTimelineService;
import com.seaman.service.DocumentRenewalDetailService;
import com.seaman.service.DocumentRenewalMobileService;
import com.seaman.service.DocumentRenewalPaymentService;
import com.seaman.service.DocumentRenewalService;
import com.seaman.service.MessageCodeService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class DocumentRenewalControllerTest {
    @Test
    void statusesDelegatesToService() {
        DocumentRenewalService renewal = mock(DocumentRenewalService.class);
        MessageCodeService messages = mock(MessageCodeService.class);
        DocumentRenewalCreateService create = mock(DocumentRenewalCreateService.class);
        DocumentRenewalItemFileService files = mock(DocumentRenewalItemFileService.class);
        DocumentRenewalResubmitService resubmit = mock(DocumentRenewalResubmitService.class);
        DocumentRenewalListService list = mock(DocumentRenewalListService.class);
        DocumentRenewalTimelineService timeline = mock(DocumentRenewalTimelineService.class);
        DocumentRenewalDetailService detail = mock(DocumentRenewalDetailService.class);
        DocumentRenewalPaymentService payment = mock(DocumentRenewalPaymentService.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        DocumentRenewalStatusResponse status = new DocumentRenewalStatusResponse();
        status.setNameEn("Pending Document Review");
        when(renewal.statuses()).thenReturn(List.of(status));
        when(messages.getMessageDescription(eq(AppStatus.SUCCESS_CODE), any())).thenReturn("Success");
        DocumentRenewalController controller = new DocumentRenewalController(
                renewal, messages, create, files, resubmit, list, timeline, detail, payment, mock(DocumentRenewalMobileService.class));

        ResponseEntity<SuccessResponse<List<DocumentRenewalStatusResponse>>> response =
                controller.statuses(request);

        assertEquals("Pending Document Review", response.getBody().getData().get(0).getNameEn());
        verify(renewal).statuses();
    }

    @Test
    void priceDelegatesDocumentCode() {
        DocumentRenewalService renewal = mock(DocumentRenewalService.class);
        MessageCodeService messages = mock(MessageCodeService.class);
        DocumentRenewalCreateService create = mock(DocumentRenewalCreateService.class);
        DocumentRenewalItemFileService files = mock(DocumentRenewalItemFileService.class);
        DocumentRenewalResubmitService resubmit = mock(DocumentRenewalResubmitService.class);
        DocumentRenewalListService list = mock(DocumentRenewalListService.class);
        DocumentRenewalTimelineService timeline = mock(DocumentRenewalTimelineService.class);
        DocumentRenewalDetailService detail = mock(DocumentRenewalDetailService.class);
        DocumentRenewalPaymentService payment = mock(DocumentRenewalPaymentService.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        DocumentRenewalPriceResponse data = new DocumentRenewalPriceResponse();
        data.setTotal(new BigDecimal("1500.00"));
        when(renewal.price("DOC001")).thenReturn(data);
        when(messages.getMessageDescription(eq(AppStatus.SUCCESS_CODE), any())).thenReturn("Success");
        DocumentRenewalController controller = new DocumentRenewalController(
                renewal, messages, create, files, resubmit, list, timeline, detail, payment, mock(DocumentRenewalMobileService.class));

        ResponseEntity<SuccessResponse<DocumentRenewalPriceResponse>> response =
                controller.price(request, "DOC001");

        assertEquals(new BigDecimal("1500.00"), response.getBody().getData().getTotal());
        verify(renewal).price("DOC001");
    }

    @Test
    void createDelegatesRequestBody() {
        DocumentRenewalService renewal = mock(DocumentRenewalService.class);
        MessageCodeService messages = mock(MessageCodeService.class);
        DocumentRenewalCreateService create = mock(DocumentRenewalCreateService.class);
        DocumentRenewalItemFileService files = mock(DocumentRenewalItemFileService.class);
        DocumentRenewalResubmitService resubmit = mock(DocumentRenewalResubmitService.class);
        DocumentRenewalListService list = mock(DocumentRenewalListService.class);
        DocumentRenewalTimelineService timeline = mock(DocumentRenewalTimelineService.class);
        DocumentRenewalDetailService detail = mock(DocumentRenewalDetailService.class);
        DocumentRenewalPaymentService payment = mock(DocumentRenewalPaymentService.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        DocumentRenewalCreateRequest input = new DocumentRenewalCreateRequest();
        input.setDocumentCode("DOC001");
        DocumentRenewalCreateResponse data = new DocumentRenewalCreateResponse();
        data.setRequestNo("260700001");
        when(create.create(input)).thenReturn(data);
        when(messages.getMessageDescription(eq(AppStatus.SUCCESS_CODE), any())).thenReturn("Success");
        DocumentRenewalController controller = new DocumentRenewalController(
                renewal, messages, create, files, resubmit, list, timeline, detail, payment, mock(DocumentRenewalMobileService.class));

        ResponseEntity<SuccessResponse<DocumentRenewalCreateResponse>> response =
                controller.create(request, input);

        assertEquals("260700001", response.getBody().getData().getRequestNo());
        verify(create).create(input);
    }

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
        DocumentRenewalPaymentService payment = mock(DocumentRenewalPaymentService.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        MultipartFile file = mock(MultipartFile.class);
        DocumentRequestItemUploadResponse data = new DocumentRequestItemUploadResponse();
        data.setProfileRequestItemId("profile-item-id");
        when(files.upload("260700001", "MRI002", "GENERAL", "MAIN", file)).thenReturn(data);
        when(messages.getMessageDescription(eq(AppStatus.SUCCESS_CODE), any())).thenReturn("Success");
        DocumentRenewalController controller =
                new DocumentRenewalController(
                        renewal, messages, create, files, resubmit, list, timeline, detail, payment, mock(DocumentRenewalMobileService.class));

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
        DocumentRenewalPaymentService payment = mock(DocumentRenewalPaymentService.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        DocumentRenewalResubmitResponse data = new DocumentRenewalResubmitResponse(
                "260700001", "PENDING_APPLICANT_CORRECTION",
                "PENDING_DOCUMENT_REVIEW", "RESUBMIT");
        when(resubmit.resubmit("260700001")).thenReturn(data);
        when(messages.getMessageDescription(eq(AppStatus.SUCCESS_CODE), any())).thenReturn("Success");
        DocumentRenewalController controller =
                new DocumentRenewalController(
                        renewal, messages, create, files, resubmit, list, timeline, detail, payment, mock(DocumentRenewalMobileService.class));

        ResponseEntity<SuccessResponse<DocumentRenewalResubmitResponse>> response =
                controller.resubmit(request, "260700001");

        assertEquals("260700001", response.getBody().getData().getRequestNo());
        verify(resubmit).resubmit("260700001");
    }

    @Test
    void updateMobileDelegatesByRequestNumberAndBody() {
        DocumentRenewalService renewal = mock(DocumentRenewalService.class);
        MessageCodeService messages = mock(MessageCodeService.class);
        DocumentRenewalCreateService create = mock(DocumentRenewalCreateService.class);
        DocumentRenewalItemFileService files = mock(DocumentRenewalItemFileService.class);
        DocumentRenewalResubmitService resubmit = mock(DocumentRenewalResubmitService.class);
        DocumentRenewalListService list = mock(DocumentRenewalListService.class);
        DocumentRenewalTimelineService timeline = mock(DocumentRenewalTimelineService.class);
        DocumentRenewalDetailService detail = mock(DocumentRenewalDetailService.class);
        DocumentRenewalPaymentService payment = mock(DocumentRenewalPaymentService.class);
        DocumentRenewalMobileService mobile = mock(DocumentRenewalMobileService.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        DocumentRenewalMobileRequest input = new DocumentRenewalMobileRequest();
        input.setMobileNumber("0821549970");
        DocumentRenewalMobileResponse data =
                new DocumentRenewalMobileResponse("260700001", "0821549970");
        when(mobile.update("260700001", input)).thenReturn(data);
        when(messages.getMessageDescription(eq(AppStatus.SUCCESS_CODE), any())).thenReturn("Success");
        DocumentRenewalController controller =
                new DocumentRenewalController(
                        renewal, messages, create, files, resubmit, list, timeline, detail, payment, mobile);

        ResponseEntity<SuccessResponse<DocumentRenewalMobileResponse>> response =
                controller.updateMobile(request, "260700001", input);

        assertEquals("0821549970", response.getBody().getData().getMobileNumber());
        verify(mobile).update("260700001", input);
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
        DocumentRenewalPaymentService payment = mock(DocumentRenewalPaymentService.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        PageDocumentRenewalResponse data = new PageDocumentRenewalResponse();
        data.setItemTotal(12);
        when(list.listMyRequests(10)).thenReturn(data);
        when(messages.getMessageDescription(eq(AppStatus.SUCCESS_CODE), any())).thenReturn("Success");
        DocumentRenewalController controller =
                new DocumentRenewalController(
                        renewal, messages, create, files, resubmit, list, timeline, detail, payment, mock(DocumentRenewalMobileService.class));

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
        DocumentRenewalPaymentService payment = mock(DocumentRenewalPaymentService.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        DocumentRenewalTimelineResponse data = new DocumentRenewalTimelineResponse();
        data.setRequestNo("260700001");
        when(timeline.timeline("260700001")).thenReturn(data);
        when(messages.getMessageDescription(eq(AppStatus.SUCCESS_CODE), any())).thenReturn("Success");
        DocumentRenewalController controller = new DocumentRenewalController(
                renewal, messages, create, files, resubmit, list, timeline, detail, payment, mock(DocumentRenewalMobileService.class));

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
        DocumentRenewalPaymentService payment = mock(DocumentRenewalPaymentService.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        DocumentRenewalDetailResponse data = new DocumentRenewalDetailResponse();
        data.setRequestNo("260700001");
        when(detail.detail("260700001")).thenReturn(data);
        when(messages.getMessageDescription(eq(AppStatus.SUCCESS_CODE), any())).thenReturn("Success");
        DocumentRenewalController controller = new DocumentRenewalController(
                renewal, messages, create, files, resubmit, list, timeline, detail, payment, mock(DocumentRenewalMobileService.class));

        ResponseEntity<SuccessResponse<DocumentRenewalDetailResponse>> response =
                controller.detail(request, "260700001");

        assertEquals("260700001", response.getBody().getData().getRequestNo());
        verify(detail).detail("260700001");
    }

    @Test
    void previewItemDelegatesByRequestNumberAndItemCode() {
        DocumentRenewalService renewal = mock(DocumentRenewalService.class);
        MessageCodeService messages = mock(MessageCodeService.class);
        DocumentRenewalCreateService create = mock(DocumentRenewalCreateService.class);
        DocumentRenewalItemFileService files = mock(DocumentRenewalItemFileService.class);
        DocumentRenewalResubmitService resubmit = mock(DocumentRenewalResubmitService.class);
        DocumentRenewalListService list = mock(DocumentRenewalListService.class);
        DocumentRenewalTimelineService timeline = mock(DocumentRenewalTimelineService.class);
        DocumentRenewalDetailService detail = mock(DocumentRenewalDetailService.class);
        DocumentRenewalPaymentService payment = mock(DocumentRenewalPaymentService.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        DocumentRenewalDetailItemResponse data = new DocumentRenewalDetailItemResponse();
        data.setDocumentRequestItemCode("MRI002");
        when(detail.previewItem("260700001", "MRI002")).thenReturn(data);
        when(messages.getMessageDescription(eq(AppStatus.SUCCESS_CODE), any())).thenReturn("Success");
        DocumentRenewalController controller = new DocumentRenewalController(
                renewal, messages, create, files, resubmit, list, timeline, detail, payment,
                mock(DocumentRenewalMobileService.class));

        ResponseEntity<SuccessResponse<DocumentRenewalDetailItemResponse>> response =
                controller.previewItem(request, "260700001", "MRI002");

        assertEquals("MRI002", response.getBody().getData().getDocumentRequestItemCode());
        verify(detail).previewItem("260700001", "MRI002");
    }

    @Test
    void previewItemByQueryDelegatesRequestNoAndMasterItemCode() {
        DocumentRenewalService renewal = mock(DocumentRenewalService.class);
        MessageCodeService messages = mock(MessageCodeService.class);
        DocumentRenewalCreateService create = mock(DocumentRenewalCreateService.class);
        DocumentRenewalItemFileService files = mock(DocumentRenewalItemFileService.class);
        DocumentRenewalResubmitService resubmit = mock(DocumentRenewalResubmitService.class);
        DocumentRenewalListService list = mock(DocumentRenewalListService.class);
        DocumentRenewalTimelineService timeline = mock(DocumentRenewalTimelineService.class);
        DocumentRenewalDetailService detail = mock(DocumentRenewalDetailService.class);
        DocumentRenewalPaymentService payment = mock(DocumentRenewalPaymentService.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        DocumentRenewalDetailItemResponse data = new DocumentRenewalDetailItemResponse();
        data.setDocumentRequestItemCode("MRI002");
        when(detail.previewItem("260700001", "MRI002")).thenReturn(data);
        when(messages.getMessageDescription(eq(AppStatus.SUCCESS_CODE), any())).thenReturn("Success");
        DocumentRenewalController controller = new DocumentRenewalController(
                renewal, messages, create, files, resubmit, list, timeline, detail, payment,
                mock(DocumentRenewalMobileService.class));

        ResponseEntity<SuccessResponse<DocumentRenewalDetailItemResponse>> response =
                controller.previewItemByQuery(request, "260700001", "MRI002");

        assertEquals("MRI002", response.getBody().getData().getDocumentRequestItemCode());
        verify(detail).previewItem("260700001", "MRI002");
    }

    @Test
    void paymentStatusDelegatesByRequestAndTransactionId() {
        DocumentRenewalService renewal = mock(DocumentRenewalService.class);
        MessageCodeService messages = mock(MessageCodeService.class);
        DocumentRenewalCreateService create = mock(DocumentRenewalCreateService.class);
        DocumentRenewalItemFileService files = mock(DocumentRenewalItemFileService.class);
        DocumentRenewalResubmitService resubmit = mock(DocumentRenewalResubmitService.class);
        DocumentRenewalListService list = mock(DocumentRenewalListService.class);
        DocumentRenewalTimelineService timeline = mock(DocumentRenewalTimelineService.class);
        DocumentRenewalDetailService detail = mock(DocumentRenewalDetailService.class);
        DocumentRenewalPaymentService payment = mock(DocumentRenewalPaymentService.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        DocumentRenewalPaymentResponse data = new DocumentRenewalPaymentResponse();
        data.setTransactionId("11111111-1111-1111-1111-111111111111");
        when(payment.status("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
                "11111111-1111-1111-1111-111111111111")).thenReturn(data);
        when(messages.getMessageDescription(eq(AppStatus.SUCCESS_CODE), any())).thenReturn("Success");
        DocumentRenewalController controller = new DocumentRenewalController(
                renewal, messages, create, files, resubmit, list, timeline, detail, payment, mock(DocumentRenewalMobileService.class));

        ResponseEntity<SuccessResponse<DocumentRenewalPaymentResponse>> response =
                controller.paymentStatus(request, "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
                        "11111111-1111-1111-1111-111111111111");

        assertEquals("11111111-1111-1111-1111-111111111111",
                response.getBody().getData().getTransactionId());
        verify(payment).status("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
                "11111111-1111-1111-1111-111111111111");
    }

    @Test
    void createPaymentDelegatesRequestAndBody() {
        DocumentRenewalService renewal = mock(DocumentRenewalService.class);
        MessageCodeService messages = mock(MessageCodeService.class);
        DocumentRenewalCreateService create = mock(DocumentRenewalCreateService.class);
        DocumentRenewalItemFileService files = mock(DocumentRenewalItemFileService.class);
        DocumentRenewalResubmitService resubmit = mock(DocumentRenewalResubmitService.class);
        DocumentRenewalListService list = mock(DocumentRenewalListService.class);
        DocumentRenewalTimelineService timeline = mock(DocumentRenewalTimelineService.class);
        DocumentRenewalDetailService detail = mock(DocumentRenewalDetailService.class);
        DocumentRenewalPaymentService payment = mock(DocumentRenewalPaymentService.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        DocumentRenewalPaymentRequest input = new DocumentRenewalPaymentRequest();
        input.setPaymentMethod("promptpay");
        input.setIdempotencyKey("renewal-260700001-1");
        DocumentRenewalPaymentResponse data = new DocumentRenewalPaymentResponse();
        data.setTransactionId("11111111-1111-1111-1111-111111111111");
        when(payment.create("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", input)).thenReturn(data);
        when(messages.getMessageDescription(eq(AppStatus.SUCCESS_CODE), any())).thenReturn("Success");
        DocumentRenewalController controller = new DocumentRenewalController(
                renewal, messages, create, files, resubmit, list, timeline, detail, payment, mock(DocumentRenewalMobileService.class));

        ResponseEntity<SuccessResponse<DocumentRenewalPaymentResponse>> response =
                controller.createPayment(request, "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", input);

        assertEquals("11111111-1111-1111-1111-111111111111",
                response.getBody().getData().getTransactionId());
        verify(payment).create("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", input);
    }
}
