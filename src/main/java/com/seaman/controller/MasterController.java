package com.seaman.controller;

import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.constant.Routes;
import com.seaman.model.common.SuccessResponse;
import com.seaman.model.response.CoursesResponse;
import com.seaman.model.response.MasterDataDocumentResponse;
import com.seaman.model.response.MasterDataResponse;
import com.seaman.service.CourseService;
import com.seaman.service.MasterDataService;
import com.seaman.service.MessageCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import static org.springframework.http.ResponseEntity.ok;

@Tag(name = "Master Data", description = "ข้อมูล Master สำหรับ Dropdown และการตั้งค่า")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
public class MasterController extends BaseController {
    private final MasterDataService masterDataService;
    private final MessageCodeService messageCodeService;

    private final CourseService courseService;

    @Operation(summary = "ข้อมูล Master", description = "ดึงข้อมูล Master ทั้งหมดสำหรับผู้ใช้ที่ login แล้ว")
    @GetMapping(Routes.MASTER)
    public ResponseEntity<SuccessResponse<MasterDataResponse>> master(HttpServletRequest httpServletRequest) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                masterDataService.list()
        ).build());
    }

    @Operation(summary = "ประเภทเอกสาร", description = "ดึงรายการประเภทเอกสารสำหรับ Dropdown ในหน้าสร้างใบรับรอง")
    @GetMapping(Routes.MASTER_DOCUMENTS)
    public ResponseEntity<SuccessResponse<MasterDataDocumentResponse>> masterDocuments(HttpServletRequest httpServletRequest) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                masterDataService.masterDataDocuments()
        ).build());
    }

    @Operation(summary = "รายการหลักสูตร", description = "ดึงรายการหลักสูตรทั้งหมดสำหรับ Dropdown")
    @GetMapping(Routes.MASTER_COURSES)
    public ResponseEntity<SuccessResponse<CoursesResponse>> masterCourses(HttpServletRequest httpServletRequest) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                courseService.courses()
        ).build());
    }

}
