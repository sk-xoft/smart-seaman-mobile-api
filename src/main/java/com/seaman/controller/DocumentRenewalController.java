package com.seaman.controller;

import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.constant.Routes;
import com.seaman.model.common.SuccessResponse;
import com.seaman.model.response.DocumentResponse;
import com.seaman.model.response.DocumentRenewalPriceResponse;
import com.seaman.model.response.DocumentRenewalStatusResponse;
import com.seaman.model.response.DocumentRequestItemUploadResponse;
import com.seaman.model.response.DocumentRenewalResubmitResponse;
import com.seaman.model.response.PageDocumentRenewalResponse;
import com.seaman.model.response.DocumentRenewalTimelineResponse;
import com.seaman.model.response.DocumentRenewalDetailResponse;
import com.seaman.model.response.DocumentRenewalDetailItemResponse;
import com.seaman.model.request.DocumentRenewalCreateRequest;
import com.seaman.model.request.DocumentRenewalMobileRequest;
import com.seaman.model.request.DocumentRenewalPaymentRequest;
import com.seaman.model.response.DocumentRenewalCreateResponse;
import com.seaman.model.response.DocumentRenewalDeleteResponse;
import com.seaman.model.response.DocumentRenewalMobileResponse;
import com.seaman.model.response.DocumentRenewalPaymentResponse;
import com.seaman.service.DocumentRenewalCreateService;
import com.seaman.service.DocumentRenewalDeleteService;
import com.seaman.service.DocumentRenewalHardDeleteService;
import com.seaman.service.DocumentRenewalItemFileService;
import com.seaman.service.DocumentRenewalResubmitService;
import com.seaman.service.DocumentRenewalListService;
import com.seaman.service.DocumentRenewalTimelineService;
import com.seaman.service.DocumentRenewalDetailService;
import com.seaman.service.DocumentRenewalMobileService;
import com.seaman.service.DocumentRenewalPaymentService;
import com.seaman.service.DocumentRenewalService;
import com.seaman.service.MessageCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import javax.validation.Valid;
import static org.springframework.http.ResponseEntity.ok;

@Tag(name = "Document Renewals", description = "Mobile document renewal APIs")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
public class DocumentRenewalController extends BaseController {
    private final DocumentRenewalService service;
    private final MessageCodeService messageCodeService;
    private final DocumentRenewalCreateService createService;
    private final DocumentRenewalItemFileService itemFileService;
    private final DocumentRenewalResubmitService resubmitService;
    private final DocumentRenewalListService listService;
    private final DocumentRenewalTimelineService timelineService;
    private final DocumentRenewalDetailService detailService;
    private final DocumentRenewalPaymentService paymentService;
    private final DocumentRenewalMobileService mobileService;
    private final DocumentRenewalDeleteService deleteService;
    private final DocumentRenewalHardDeleteService hardDeleteService;

    @Operation(summary = "Create unpaid document renewal draft")
    @PostMapping(Routes.DOCUMENT_RENEWALS)
    public ResponseEntity<SuccessResponse<DocumentRenewalCreateResponse>> create(
            HttpServletRequest request, @Valid @RequestBody DocumentRenewalCreateRequest input) {
        return ok(success(request, createService.create(input)));
    }

    @Operation(summary = "Replace a supporting file requested for correction")
    @PostMapping(value = Routes.DOCUMENT_RENEWAL_ITEM_FILE,
            consumes = "multipart/form-data")
    public ResponseEntity<SuccessResponse<DocumentRequestItemUploadResponse>> uploadItemFile(
            HttpServletRequest request,
            @PathVariable String requestNo,
            @PathVariable String documentRequestItemCode,
            @RequestParam String documentType,
            @RequestParam String slotCode,
            @RequestPart("file") MultipartFile file) {
        return ok(success(request, itemFileService.upload(
                requestNo, documentRequestItemCode, documentType, slotCode, file)));
    }

    @Operation(summary = "Resubmit corrected renewal documents")
    @PostMapping(Routes.DOCUMENT_RENEWAL_RESUBMIT)
    public ResponseEntity<SuccessResponse<DocumentRenewalResubmitResponse>> resubmit(
            HttpServletRequest request, @PathVariable String requestNo) {
        return ok(success(request, resubmitService.resubmit(requestNo)));
    }

    @Operation(summary = "Update renewal request mobile number snapshot")
    @PutMapping(Routes.DOCUMENT_RENEWAL_REQUEST_MOBILE)
    public ResponseEntity<SuccessResponse<DocumentRenewalMobileResponse>> updateMobile(
            HttpServletRequest request,
            @PathVariable String requestNo,
            @Valid @RequestBody DocumentRenewalMobileRequest input) {
        return ok(success(request, mobileService.update(requestNo, input)));
    }

    @Operation(summary = "Delete an unpaid document renewal request")
    @DeleteMapping(Routes.DOCUMENT_RENEWAL_REQUEST_DELETE)
    public ResponseEntity<SuccessResponse<DocumentRenewalDeleteResponse>> delete(
            HttpServletRequest request, @PathVariable String requestNo) {
        return ok(success(request, deleteService.delete(requestNo)));
    }

    @Operation(summary = "Permanently delete a renewal request and its related records")
    @DeleteMapping(Routes.DOCUMENT_RENEWAL_REQUEST_HARD_DELETE)
    public ResponseEntity<SuccessResponse<DocumentRenewalDeleteResponse>> hardDelete(
            HttpServletRequest request, @PathVariable String requestNo) {
        return ok(success(request, hardDeleteService.hardDelete(requestNo)));
    }

    @Operation(summary = "Create an Omise payment attempt for a renewal request")
    @PostMapping(Routes.DOCUMENT_RENEWAL_PAYMENTS)
    public ResponseEntity<SuccessResponse<DocumentRenewalPaymentResponse>> createPayment(
            HttpServletRequest request,
            @PathVariable String requestId,
            @Valid @RequestBody DocumentRenewalPaymentRequest input) {
        return ok(success(request, paymentService.create(requestId, input)));
    }

    @Operation(summary = "Get a renewal payment status")
    @GetMapping(Routes.DOCUMENT_RENEWAL_PAYMENT)
    public ResponseEntity<SuccessResponse<DocumentRenewalPaymentResponse>> paymentStatus(
            HttpServletRequest request,
            @PathVariable String requestId,
            @PathVariable String transactionId) {
        return ok(success(request, paymentService.status(requestId, transactionId)));
    }

    @Operation(summary = "List the current user's document renewal requests")
    @GetMapping(Routes.DOCUMENT_RENEWALS_MY)
    public ResponseEntity<SuccessResponse<PageDocumentRenewalResponse>> myRequests(
            HttpServletRequest request, @RequestParam int offSet) {
        return ok(success(request, listService.listMyRequests(offSet)));
    }

    @Operation(summary = "Get the current user's document renewal timeline")
    @GetMapping(Routes.DOCUMENT_RENEWAL_TIMELINE)
    public ResponseEntity<SuccessResponse<DocumentRenewalTimelineResponse>> timeline(
            HttpServletRequest request, @PathVariable String requestNo) {
        return ok(success(request, timelineService.timeline(requestNo)));
    }

    @Operation(summary = "Get the current user's document renewal request detail")
    @GetMapping(Routes.DOCUMENT_RENEWAL_DETAIL)
    public ResponseEntity<SuccessResponse<DocumentRenewalDetailResponse>> detail(
            HttpServletRequest request, @PathVariable String requestNo) {
        return ok(success(request, detailService.detail(requestNo)));
    }

    @Operation(summary = "Preview one renewal supporting document item")
    @GetMapping(Routes.DOCUMENT_RENEWAL_ITEM_PREVIEW)
    public ResponseEntity<SuccessResponse<DocumentRenewalDetailItemResponse>> previewItem(
            HttpServletRequest request,
            @PathVariable String requestNo,
            @PathVariable String documentRequestItemCode) {
        return ok(success(request, detailService.previewItem(
                requestNo, documentRequestItemCode)));
    }

    @Operation(summary = "Preview one renewal supporting document item by request parameters")
    @GetMapping(Routes.DOCUMENT_RENEWAL_ITEM_PREVIEW_BY_QUERY)
    public ResponseEntity<SuccessResponse<DocumentRenewalDetailItemResponse>> previewItemByQuery(
            HttpServletRequest request,
            @RequestParam("request_no") String requestNo,
            @RequestParam("request_master_items_code") String documentRequestItemCode) {
        return ok(success(request, detailService.previewItem(
                requestNo, documentRequestItemCode)));
    }

    @Operation(summary = "Get active renewal statuses")
    @GetMapping(Routes.DOCUMENT_RENEWAL_STATUSES)
    public ResponseEntity<SuccessResponse<List<DocumentRenewalStatusResponse>>> statuses(HttpServletRequest request) {
        return ok(success(request, service.statuses()));
    }

    @Operation(summary = "List documents available for renewal")
    @GetMapping({Routes.DOCUMENT_RENEWALS, Routes.DOCUMENT_RENEWALS})
    public ResponseEntity<SuccessResponse<List<DocumentResponse>>> documents(HttpServletRequest request) {
        return ok(success(request, service.documents()));
    }

    @Operation(summary = "Get active document renewal price")
    @GetMapping(Routes.DOCUMENT_RENEWAL_PRICES)
    public ResponseEntity<SuccessResponse<DocumentRenewalPriceResponse>> price(
            HttpServletRequest request, @RequestParam String documentCode) {
        return ok(success(request, service.price(documentCode)));
    }

    private <T> SuccessResponse<T> success(HttpServletRequest request, T data) {
        String description = messageCodeService.getMessageDescription(
                AppStatus.SUCCESS_CODE, (String) request.getAttribute(AppSys.LANGUAGE));
        return SuccessResponse.<T>builder(AppStatus.SUCCESS_CODE, description, data).build();
    }
}
