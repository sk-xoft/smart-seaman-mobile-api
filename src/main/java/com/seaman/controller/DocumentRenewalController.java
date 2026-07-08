package com.seaman.controller;

import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.constant.Routes;
import com.seaman.model.common.SuccessResponse;
import com.seaman.model.response.DocumentRenewalPriceResponse;
import com.seaman.model.response.DocumentRenewalStatusResponse;
import com.seaman.model.response.DocumentRequestItemUploadResponse;
import com.seaman.model.response.DocumentRenewalResubmitResponse;
import com.seaman.model.request.DocumentRenewalCreateRequest;
import com.seaman.model.response.DocumentRenewalCreateResponse;
import com.seaman.service.DocumentRenewalCreateService;
import com.seaman.service.DocumentRenewalItemFileService;
import com.seaman.service.DocumentRenewalResubmitService;
import com.seaman.service.DocumentRenewalService;
import com.seaman.service.MessageCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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

    @Operation(summary = "Get active renewal statuses")
    @GetMapping(Routes.DOCUMENT_RENEWAL_STATUSES)
    public ResponseEntity<SuccessResponse<List<DocumentRenewalStatusResponse>>> statuses(HttpServletRequest request) {
        return ok(success(request, service.statuses()));
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
