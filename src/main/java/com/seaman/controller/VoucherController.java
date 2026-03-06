package com.seaman.controller;

import com.google.zxing.WriterException;
import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.constant.Routes;
import com.seaman.model.common.SuccessResponse;
import com.seaman.model.response.VoucherModel;
import com.seaman.model.response.VoucherResponse;
import com.seaman.service.DeleteUserMobileService;
import com.seaman.service.MessageCodeService;
import com.seaman.service.VoucherService;
import lombok.RequiredArgsConstructor;
import net.sf.jmimemagic.*;
import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.io.IOUtils;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import static org.springframework.http.ResponseEntity.ok;

@RequiredArgsConstructor
@RestController
public class VoucherController extends BaseController {

    private final MessageCodeService messageCodeService;

    private final VoucherService voucherService;

    @GetMapping(Routes.VOUCHERS)
    public ResponseEntity<SuccessResponse<VoucherResponse>> listNews(HttpServletRequest httpServletRequest) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                voucherService.listVoucher()
        ).build());
    }

    @GetMapping(Routes.VOUCHERS_DETAIL)
    public ResponseEntity<SuccessResponse<VoucherModel>> voucherDetail(HttpServletRequest httpServletRequest, @RequestParam("voucherId") String voucherId) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                voucherService.voucherDetail(voucherId)
        ).build());
    }

//    @GetMapping(Routes.VOUCHERS_PREVIEW)
//    @ResponseStatus(HttpStatus.OK)
//    public HttpEntity<byte[]> getImageVoucher(@RequestParam("voucherId") String voucherId) throws MagicMatchNotFoundException, MagicException, MagicParseException, IOException {
//
//        String fileBase64 = voucherService.previewVoucher(voucherId);
//
//        // 1. download img your location...
//        byte[] content = Base64.getDecoder().decode(fileBase64);
//
//        MagicMatch match = Magic.getMagicMatch(content);
//        String mimeType = match.getMimeType();
//        HttpHeaders headers = new HttpHeaders();
//
//        if("image/png".equals(mimeType)) {
//            headers.setContentType(MediaType.IMAGE_PNG);
//        }
//
//        if("image/jpeg".equals(mimeType)) {
//            headers.setContentType(MediaType.IMAGE_JPEG);
//        }
//
//        headers.setContentLength(content.length);
//        return new HttpEntity<byte[]>(content, headers);
//    }

    @GetMapping(value = Routes.VOUCHERS_PREVIEW, produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getImage(@RequestParam("voucherId") String voucherId) {

        // Replace with your Base64 encoded image string
        String base64Image = voucherService.previewVoucher(voucherId);
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentLength(imageBytes.length);

        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
    }

    @GetMapping(Routes.VOUCHERS_PREVIEW_QR)
    @ResponseStatus(HttpStatus.OK)
    public HttpEntity<byte[]> getQR(@RequestParam("voucherId") String voucherId) throws IOException, WriterException, MagicMatchNotFoundException, MagicException, MagicParseException {

        String fileBase64 = voucherService.previewQrCode(voucherId);

        // 1. download img your location...
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

//    private final DeleteUserMobileService deleteUserMobileService;
//    @GetMapping("/test")
//    public String test(){
//        deleteUserMobileService.deleteUserIsOverDueDate();
//        return "Success";
//    }
}
