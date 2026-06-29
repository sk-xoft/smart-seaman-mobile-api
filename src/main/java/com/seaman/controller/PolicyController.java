package com.seaman.controller;


import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.constant.Routes;
import com.seaman.model.common.SuccessResponse;
import com.seaman.service.MessageCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import static org.springframework.http.ResponseEntity.ok;

@Tag(name = "Policy", description = "นโยบายการใช้งานและเงื่อนไขการให้บริการ")
@RestController
@RequiredArgsConstructor
public class PolicyController {

    private final MessageCodeService messageCodeService;

    @Operation(summary = "ข้อมูลนโยบายการใช้งาน", description = "ดึงข้อมูลนโยบายความเป็นส่วนตัวและเงื่อนไขการให้บริการ")
    @GetMapping(Routes.POLICY)
    public ResponseEntity<SuccessResponse<String>> activateUser(HttpServletRequest httpServletRequest) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                "success"
        ).build());
    }

}
