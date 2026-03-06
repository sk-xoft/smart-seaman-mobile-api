package com.seaman.controller;

import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.constant.Routes;
import com.seaman.model.common.SuccessResponse;
import com.seaman.model.response.PageDocumentResponse;
import com.seaman.model.response.SchoolTrainingResponse;
import com.seaman.service.MessageCodeService;
import com.seaman.service.SchoolTrainingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequiredArgsConstructor
public class SchoolController extends BaseController {

    private final MessageCodeService messageCodeService;

    private final SchoolTrainingService schoolTrainingService;

    @GetMapping(Routes.SCHOOL_TRAINING_LIST)
    public ResponseEntity<SuccessResponse<SchoolTrainingResponse>> listSchoolTraining(HttpServletRequest httpServletRequest, @RequestParam("courseCode") String courseCode) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                schoolTrainingService.listSchoolTraining(httpServletRequest, courseCode)
        ).build());
    }

    @GetMapping(Routes.SCHOOL_TRAINING_DETAIL)
    public ResponseEntity<SuccessResponse<SchoolTrainingResponse>> schoolTrainingDetail(HttpServletRequest httpServletRequest, @RequestParam("companyCode") String companyCode, @RequestParam("courseCode") String courseCode) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                schoolTrainingService.schoolTrainingDetail(httpServletRequest, companyCode, courseCode)
        ).build());
    }



}
