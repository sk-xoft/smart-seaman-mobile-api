package com.seaman.controller;

import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.constant.Routes;
import com.seaman.model.common.SuccessResponse;
import com.seaman.service.EmailService;
import com.seaman.service.MessageCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequiredArgsConstructor
public class EmailController extends BaseController {

    private final MessageCodeService messageCodeService;
    private final EmailService emailService;

    /**
     * For
     * @param httpServletRequest
     * @return
     */
    @GetMapping(Routes.SEND_EMAIL)
    public ResponseEntity<SuccessResponse<String>> sendEmail(HttpServletRequest httpServletRequest) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        String fullName = "ทดสอบ";
        String email = "sarunyoo.xoftspace@gmail.com";
        String linkRegister = "www.google.com";

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                emailService.sendEmailRegister(fullName, email, linkRegister, "")
        ).build());
    }
}
