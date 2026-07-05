package com.seaman.controller;

import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.constant.Routes;
import com.seaman.model.common.SuccessResponse;
import com.seaman.model.response.DocumentRenewalPriceResponse;
import com.seaman.model.response.DocumentRenewalStatusResponse;
import com.seaman.model.request.DocumentRenewalCreateRequest;
import com.seaman.model.response.DocumentRenewalCreateResponse;
import com.seaman.service.DocumentRenewalCreateService;
import com.seaman.service.DocumentRenewalService;
import com.seaman.service.MessageCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @Operation(summary = "Create unpaid document renewal draft")
    @PostMapping(Routes.DOCUMENT_RENEWALS)
    public ResponseEntity<SuccessResponse<DocumentRenewalCreateResponse>> create(
            HttpServletRequest request, @Valid @RequestBody DocumentRenewalCreateRequest input) {
        return ok(success(request, createService.create(input)));
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
