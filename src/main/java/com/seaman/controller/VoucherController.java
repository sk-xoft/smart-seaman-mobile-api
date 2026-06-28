package com.seaman.controller;

import com.google.zxing.WriterException;
import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.constant.Routes;
import com.seaman.model.common.SuccessResponse;
import com.seaman.model.response.VoucherModel;
import com.seaman.model.response.VoucherResponse;
import com.seaman.service.MessageCodeService;
import com.seaman.service.VoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import net.sf.jmimemagic.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Base64;
import static org.springframework.http.ResponseEntity.ok;

@Tag(name = "Vouchers", description = "คูปองและสิทธิพิเศษ")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@RestController
public class VoucherController extends BaseController {

    private final MessageCodeService messageCodeService;

    private final VoucherService voucherService;

    @Operation(summary = "รายการ Voucher", description = "ดึงรายการ Voucher และคูปองทั้งหมด")
    @GetMapping(Routes.VOUCHERS)
    public ResponseEntity<SuccessResponse<VoucherResponse>> listNews(HttpServletRequest httpServletRequest) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                voucherService.listVoucher()
        ).build());
    }

    @Operation(summary = "รายละเอียด Voucher", description = "ดึงรายละเอียดของ Voucher ตาม ID")
    @GetMapping(Routes.VOUCHERS_DETAIL)
    public ResponseEntity<SuccessResponse<VoucherModel>> voucherDetail(HttpServletRequest httpServletRequest,
            @Parameter(description = "Voucher ID", required = true) @RequestParam("voucherId") String voucherId) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                voucherService.voucherDetail(voucherId)
        ).build());
    }

    @Operation(summary = "รูปภาพ Voucher", description = "ดาวน์โหลดรูปภาพของ Voucher")
    @GetMapping(value = Routes.VOUCHERS_PREVIEW, produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getImage(
            @Parameter(description = "Voucher ID", required = true) @RequestParam("voucherId") String voucherId) {

        String base64Image = voucherService.previewVoucher(voucherId);
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentLength(imageBytes.length);

        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
    }

    @Operation(summary = "QR Code Voucher", description = "ดาวน์โหลด QR Code สำหรับแลก Voucher")
    @GetMapping(Routes.VOUCHERS_PREVIEW_QR)
    @ResponseStatus(HttpStatus.OK)
    public HttpEntity<byte[]> getQR(
            @Parameter(description = "Voucher ID", required = true) @RequestParam("voucherId") String voucherId) throws IOException, WriterException, MagicMatchNotFoundException, MagicException, MagicParseException {

        String fileBase64 = voucherService.previewQrCode(voucherId);

        byte[] content = Base64.getDecoder().decode(fileBase64);

        MagicMatch match = Magic.getMagicMatch(content);

        String mimeType = match.getMimeType();
        HttpHeaders headers = new HttpHeaders();

        if("image/png".equals(mimeType)) {
            headers.setContentType(MediaType.IMAGE_PNG);
        }

        if("image/jpeg".equals(mimeType)) {
            headers.setContentType(MediaType.IMAGE_JPEG);
        }

        headers.setContentLength(content.length);
        return new HttpEntity<byte[]>(content, headers);
    }
}
