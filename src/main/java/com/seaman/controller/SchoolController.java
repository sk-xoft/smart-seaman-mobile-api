package com.seaman.controller;

import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.constant.Routes;
import com.seaman.model.common.SuccessResponse;
import com.seaman.model.response.SchoolTrainingResponse;
import com.seaman.service.MessageCodeService;
import com.seaman.service.SchoolTrainingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.ResponseEntity.ok;

@Tag(name = "School & Training", description = "สถาบันฝึกอบรมและหลักสูตร")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
public class SchoolController extends BaseController {

    private final MessageCodeService messageCodeService;

    private final SchoolTrainingService schoolTrainingService;

    @Operation(summary = "รายการสถาบันฝึกอบรม", description = "ดึงรายการสถาบันฝึกอบรมที่เปิดสอนหลักสูตรที่ระบุ")
    @GetMapping(Routes.SCHOOL_TRAINING_LIST)
    public ResponseEntity<SuccessResponse<SchoolTrainingResponse>> listSchoolTraining(HttpServletRequest httpServletRequest,
            @Parameter(description = "รหัสหลักสูตร", required = true) @RequestParam("courseCode") String courseCode) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                schoolTrainingService.listSchoolTraining(httpServletRequest, courseCode)
        ).build());
    }

    @Operation(summary = "รายละเอียดสถาบัน", description = "ดึงรายละเอียดของสถาบันฝึกอบรมสำหรับหลักสูตรที่ระบุ")
    @GetMapping(Routes.SCHOOL_TRAINING_DETAIL)
    public ResponseEntity<SuccessResponse<SchoolTrainingResponse>> schoolTrainingDetail(HttpServletRequest httpServletRequest,
            @Parameter(description = "รหัสบริษัท/สถาบัน", required = true) @RequestParam("companyCode") String companyCode,
            @Parameter(description = "รหัสหลักสูตร", required = true) @RequestParam("courseCode") String courseCode) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                schoolTrainingService.schoolTrainingDetail(httpServletRequest, companyCode, courseCode)
        ).build());
    }



}
