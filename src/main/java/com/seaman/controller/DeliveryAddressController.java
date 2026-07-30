package com.seaman.controller;

import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.constant.Routes;
import com.seaman.model.common.SuccessResponse;
import com.seaman.model.request.DeliveryAddressRequest;
import com.seaman.model.response.DeliveryAddressResponse;
import com.seaman.service.DeliveryAddressService;
import com.seaman.service.MessageCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import static org.springframework.http.ResponseEntity.ok;

@Tag(name = "Delivery Address", description = "จัดการที่อยู่จัดส่งของผู้ใช้")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
public class DeliveryAddressController extends BaseController {

    private final DeliveryAddressService deliveryAddressService;
    private final MessageCodeService messageCodeService;

    @Operation(summary = "ดึงที่อยู่จัดส่งหลักของผู้ใช้")
    @GetMapping(Routes.DELIVERY_ADDRESSES)
    public ResponseEntity<SuccessResponse<DeliveryAddressResponse>> getDefault(
            HttpServletRequest httpRequest) {
        return ok(success(httpRequest, deliveryAddressService.getDefault()));
    }

    @Operation(summary = "สร้างที่อยู่จัดส่ง")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "สร้างที่อยู่สำเร็จ"),
            @ApiResponse(responseCode = "400", description = "Request validation error")
    })
    @PostMapping(Routes.DELIVERY_ADDRESSES)
    public ResponseEntity<SuccessResponse<DeliveryAddressResponse>> create(
            HttpServletRequest httpRequest, @Valid @RequestBody DeliveryAddressRequest request) {
        return ok(success(httpRequest, deliveryAddressService.create(request)));
    }

    @Operation(summary = "สร้างที่อยู่จัดส่งสำหรับ renewal request")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "สร้างที่อยู่สำหรับ renewal สำเร็จ"),
            @ApiResponse(responseCode = "400", description = "Request validation error")
    })
    @PostMapping(Routes.DELIVERY_ADDRESSES_RENEWAL)
    public ResponseEntity<SuccessResponse<DeliveryAddressResponse>> createForRenewal(
            HttpServletRequest httpRequest, @PathVariable String requestNo,
            @Valid @RequestBody DeliveryAddressRequest request) {
        return ok(success(httpRequest, deliveryAddressService.createForRenewal(requestNo, request)));
    }

    @Operation(summary = "แก้ไขที่อยู่จัดส่ง")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "แก้ไขที่อยู่สำเร็จ"),
            @ApiResponse(responseCode = "400", description = "Request validation error")
    })
    @PutMapping(Routes.DELIVERY_ADDRESSES_RENEWAL)
    public ResponseEntity<SuccessResponse<DeliveryAddressResponse>> update(
            HttpServletRequest httpRequest, @PathVariable String addressId,
            @Valid @RequestBody DeliveryAddressRequest request) {
        return ok(success(httpRequest, deliveryAddressService.update(addressId, request)));
    }

    private SuccessResponse<DeliveryAddressResponse> success(
            HttpServletRequest request, DeliveryAddressResponse data) {
        String description = messageCodeService.getMessageDescription(
                AppStatus.SUCCESS_CODE, (String) request.getAttribute(AppSys.LANGUAGE));
        return SuccessResponse.<DeliveryAddressResponse>builder(
                AppStatus.SUCCESS_CODE, description, data).build();
    }
}
