package com.seaman.service;

import com.seaman.constant.AppStatus;
import com.seaman.entity.CourseEntity;
import com.seaman.exception.BusinessException;
import com.seaman.model.response.CourseListResponse;
import com.seaman.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock CourseRepository courseRepository;

    private CourseService service;

    @BeforeEach
    void setUp() {
        service = new CourseService(courseRepository);
    }

    @Test
    void coursesMapsEntitiesToResponses() {
        CourseEntity entity = new CourseEntity();
        entity.setCourseCode("COURSE1");
        entity.setCourseNameEn("Course EN");
        entity.setCourseNameTh("Course TH");
        when(courseRepository.findAll()).thenReturn(List.of(entity));

        CourseListResponse response = service.courses();

        assertEquals(1, response.getCourses().size());
        assertEquals("COURSE1", response.getCourses().get(0).getCourseCode());
        assertEquals("Course EN", response.getCourses().get(0).getCourseNameEn());
        assertEquals("Course TH", response.getCourses().get(0).getCourseNameTh());
    }

    @Test
    void coursesReturnsEmptyListWhenNoneFound() {
        when(courseRepository.findAll()).thenReturn(Collections.emptyList());

        CourseListResponse response = service.courses();

        assertTrue(response.getCourses().isEmpty());
    }

    @Test
    void coursesRethrowsCommonExceptionAsIs() {
        when(courseRepository.findAll()).thenThrow(new BusinessException(AppStatus.EXCEPTION_DATABASE, "db error"));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.courses());
        assertEquals(AppStatus.EXCEPTION_DATABASE, ex.getCode());
    }

    @Test
    void coursesWrapsGenericExceptionAsBusinessException() {
        when(courseRepository.findAll()).thenThrow(new RuntimeException("boom"));

        BusinessException ex = assertThrows(BusinessException.class, () -> service.courses());
        assertEquals(AppStatus.EXCEPTION_GLOBAL, ex.getCode());
        assertEquals("boom", ex.getMessage());
    }
}
