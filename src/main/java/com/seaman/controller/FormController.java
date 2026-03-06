package com.seaman.controller;

import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.constant.Routes;
import com.seaman.model.common.SuccessResponse;
import com.seaman.model.response.FormResponse;
import com.seaman.service.FormService;
import com.seaman.service.MessageCodeService;
import lombok.RequiredArgsConstructor;
import net.sf.jmimemagic.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import java.util.Base64;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequiredArgsConstructor
public class FormController extends BaseController {

    private final MessageCodeService messageCodeService;

    private final FormService formService;

    @GetMapping(Routes.FORM_LIST)
    public ResponseEntity<SuccessResponse<FormResponse>> listSchoolTraining(HttpServletRequest httpServletRequest) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                formService.formAll(httpServletRequest)
        ).build());
    }

    @GetMapping(Routes.FORM_BY_CODE)
    @ResponseStatus(HttpStatus.OK)
    public HttpEntity<byte[]> getImage(HttpServletRequest httpServletRequest, @RequestParam("formId") String formId) throws MagicMatchNotFoundException, MagicException, MagicParseException {


        // 1. download img your location...
        byte[] content = formService.downloadForm(httpServletRequest, formId);

        MagicMatch match = Magic.getMagicMatch(content);
        String mimeType = match.getMimeType();
        HttpHeaders headers = new HttpHeaders();

//        if("image/png".equals(mimeType)) {
//            headers.setContentType(MediaType.IMAGE_PNG);
//        }
//
//        if("image/jpeg".equals(mimeType)) {
//            headers.setContentType(MediaType.IMAGE_JPEG);
//        }
//
//        if("application/pdf".equals(mimeType)) {
            headers.setContentType(MediaType.APPLICATION_PDF);
//        }

        headers.setContentLength(content.length);

        return new HttpEntity<byte[]>(content, headers);
    }
}
