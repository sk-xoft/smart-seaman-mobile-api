package com.seaman.controller;

import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.constant.Routes;
import com.seaman.model.common.SuccessResponse;
import com.seaman.model.response.FormResponse;
import com.seaman.service.FormService;
import com.seaman.service.MessageCodeService;
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

import static org.springframework.http.ResponseEntity.ok;

@Tag(name = "Forms", description = "แบบฟอร์มและเอกสาร PDF")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
public class FormController extends BaseController {

    private final MessageCodeService messageCodeService;

    private final FormService formService;

    @Operation(summary = "รายการแบบฟอร์ม", description = "ดึงรายการแบบฟอร์มและเอกสารทั้งหมด")
    @GetMapping(Routes.FORM_LIST)
    public ResponseEntity<SuccessResponse<FormResponse>> listSchoolTraining(HttpServletRequest httpServletRequest) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                formService.formAll(httpServletRequest)
        ).build());
    }

    @Operation(summary = "ดาวน์โหลด PDF แบบฟอร์ม", description = "ดาวน์โหลดไฟล์ PDF ของแบบฟอร์มตาม ID")
    @GetMapping(Routes.FORM_BY_CODE)
    @ResponseStatus(HttpStatus.OK)
    public HttpEntity<byte[]> getImage(HttpServletRequest httpServletRequest,
            @Parameter(description = "Form ID", required = true) @RequestParam("formId") String formId) throws MagicMatchNotFoundException, MagicException, MagicParseException {

        byte[] content = formService.downloadForm(httpServletRequest, formId);

        MagicMatch match = Magic.getMagicMatch(content);
        String mimeType = match.getMimeType();
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_PDF);

        headers.setContentLength(content.length);

        return new HttpEntity<byte[]>(content, headers);
    }
}
