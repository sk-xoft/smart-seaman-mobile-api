package com.seaman.controller;

import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.constant.Routes;
import com.seaman.model.common.SuccessResponse;
import com.seaman.model.response.CoursesResponse;
import com.seaman.model.response.DocumentResponse;
import com.seaman.model.response.MasterDataDocumentResponse;
import com.seaman.model.response.MasterDataResponse;
import com.seaman.service.CourseService;
import com.seaman.service.MasterDataService;
import com.seaman.service.MessageCodeService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequiredArgsConstructor
public class MasterController extends BaseController {
    private final MasterDataService masterDataService;
    private final MessageCodeService messageCodeService;

    private final CourseService courseService;

    /**
     * this master for user mobile is has register.
     * @param httpServletRequest
     * @return
     */
    @GetMapping(Routes.MASTER)
    public ResponseEntity<SuccessResponse<MasterDataResponse>> master(HttpServletRequest httpServletRequest) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                masterDataService.list()
        ).build());
    }

    /**
     * this list data for use dropdown on create certification.
     * @param httpServletRequest
     * @return
     */
    @GetMapping(Routes.MASTER_DOCUMENTS)
    public ResponseEntity<SuccessResponse<MasterDataDocumentResponse>> masterDocuments(HttpServletRequest httpServletRequest) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                masterDataService.masterDataDocuments()
        ).build());
    }

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
