package com.seaman.service;

import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.entity.CourseEntity;
import com.seaman.entity.DocumentEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.exception.BusinessException;
import com.seaman.exception.CommonException;
import com.seaman.model.response.CourseListResponse;
import com.seaman.model.response.CoursesResponse;
import com.seaman.model.response.PageDocumentResponse;
import com.seaman.repository.CourseRepository;
import com.seaman.utils.DateUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Period;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final CourseRepository courseRepository;

    public CourseListResponse courses() {

        CourseListResponse response = new CourseListResponse();
        List<CoursesResponse> coursesResponses = new ArrayList<>();

        try {

            List<CourseEntity> courseEntities = courseRepository.findAll();

            for(CourseEntity item: courseEntities) {
                CoursesResponse model = new CoursesResponse();
                model.setCourseCode(item.getCourseCode());
                model.setCourseNameEn(item.getCourseNameEn());
                model.setCourseNameTh(item.getCourseNameTh());
                coursesResponses.add(model);
            }

            response.setCourses(coursesResponses);

        } catch (CommonException ce){
            log.error("{} error -> {}", "Course", ce);
            throw  ce;
        } catch(Exception ex){
            log.error("{} error -> {}", "Course", ex);
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());
        }

        return response;
    }

}
