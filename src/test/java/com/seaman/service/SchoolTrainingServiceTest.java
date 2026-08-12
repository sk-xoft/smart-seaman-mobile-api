package com.seaman.service;

import com.seaman.entity.ListSchoolTrainingEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.model.response.SchoolTrainingResponse;
import com.seaman.repository.SchoolTrainingRepository;
import com.seaman.utils.DateUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchoolTrainingServiceTest {

    @Mock SchoolTrainingRepository schoolTrainingRepository;
    @Mock TransactionLogsService transactionLogsService;
    @Mock DateUtil dateUtil;
    @Mock HttpServletRequest httpServletRequest;

    private SchoolTrainingService service;
    private UsersEntity user;

    @BeforeEach
    void setUp() {
        service = new SchoolTrainingService(schoolTrainingRepository, transactionLogsService, dateUtil);

        user = new UsersEntity();
        user.setUsername("crew@example.com");
        lenient().when(httpServletRequest.getAttribute(anyString()))
                .thenAnswer(invocation -> "userObject".equals(invocation.getArgument(0)) ? user : null);
    }

    // ---- listSchoolTraining

    @Test
    void listSchoolTrainingDeduplicatesByCompanyAndCourse() {
        ListSchoolTrainingEntity first = trainingEntity("COMP1", "COURSE1", "20260101");
        ListSchoolTrainingEntity duplicate = trainingEntity("COMP1", "COURSE1", "20260102");
        when(schoolTrainingRepository.listSchoolTrainings("COURSE1")).thenReturn(List.of(first, duplicate));

        SchoolTrainingResponse response = service.listSchoolTraining(httpServletRequest, "COURSE1");

        assertEquals(1, response.getSchoolTrainings().size());
    }

    @Test
    void listSchoolTrainingComputesStartEndDatesFromOnlineAndOnsiteDates() {
        ListSchoolTrainingEntity entity = trainingEntity("COMP1", "COURSE1", "20260101");
        entity.setCourseOnlineDate("01/01/2026");
        entity.setCourseOnsiteDate("05/01/2026, 03/01/2026");
        when(schoolTrainingRepository.listSchoolTrainings("COURSE1")).thenReturn(List.of(entity));
        when(dateUtil.formatStrToStr(anyString(), eq(DateUtil.DDMMYYYY))).thenAnswer(inv -> {
            String input = inv.getArgument(0);
            // normalize dd/MM/yyyy -> yyyyMMdd for sortability, mimicking real formatting behavior
            String[] parts = input.split("/");
            return parts[2] + parts[1] + parts[0];
        });
        when(dateUtil.formatStrToStrDDMMYYYY(anyString(), eq(DateUtil.YYYYMMDD))).thenAnswer(inv -> {
            String input = inv.getArgument(0);
            return input.substring(6, 8) + "/" + input.substring(4, 6) + "/" + input.substring(0, 4);
        });

        SchoolTrainingResponse response = service.listSchoolTraining(httpServletRequest, "COURSE1");

        ListSchoolTrainingEntity result = response.getSchoolTrainings().get(0);
        assertEquals("01/01/2026", result.getCourseStartDate());
        assertEquals("05/01/2026", result.getCourseEndDate());
    }

    @Test
    void listSchoolTrainingLeavesDatesEmptyWhenNoDatesProvided() {
        ListSchoolTrainingEntity entity = trainingEntity("COMP1", "COURSE1", "20260101");
        entity.setCourseOnlineDate(null);
        entity.setCourseOnsiteDate("");
        when(schoolTrainingRepository.listSchoolTrainings("COURSE1")).thenReturn(List.of(entity));

        SchoolTrainingResponse response = service.listSchoolTraining(httpServletRequest, "COURSE1");

        ListSchoolTrainingEntity result = response.getSchoolTrainings().get(0);
        assertEquals("", result.getCourseStartDate());
        assertEquals("", result.getCourseEndDate());
        verify(dateUtil, never()).formatStrToStr(anyString(), anyString());
    }

    @Test
    void listSchoolTrainingHandlesEmptyRepositoryResult() {
        when(schoolTrainingRepository.listSchoolTrainings("COURSE1")).thenReturn(Collections.emptyList());

        SchoolTrainingResponse response = service.listSchoolTraining(httpServletRequest, "COURSE1");

        assertTrue(response.getSchoolTrainings().isEmpty());
    }

    // ---- schoolTrainingDetail

    @Test
    void schoolTrainingDetailReturnsRepositoryResultAsIs() {
        ListSchoolTrainingEntity entity = trainingEntity("COMP1", "COURSE1", "20260101");
        when(schoolTrainingRepository.schoolTrainingsDetail("COMP1", "COURSE1")).thenReturn(List.of(entity));

        SchoolTrainingResponse response = service.schoolTrainingDetail(httpServletRequest, "COMP1", "COURSE1");

        assertEquals(1, response.getSchoolTrainings().size());
        assertEquals("COMP1", response.getSchoolTrainings().get(0).getCompanyCode());
    }

    @Test
    void schoolTrainingDetailHandlesEmptyResult() {
        when(schoolTrainingRepository.schoolTrainingsDetail("COMP1", "COURSE1")).thenReturn(Collections.emptyList());

        SchoolTrainingResponse response = service.schoolTrainingDetail(httpServletRequest, "COMP1", "COURSE1");

        assertTrue(response.getSchoolTrainings().isEmpty());
    }

    // ---- fixtures

    private ListSchoolTrainingEntity trainingEntity(String companyCode, String courseCode, String dateForCheck) {
        ListSchoolTrainingEntity entity = new ListSchoolTrainingEntity();
        entity.setCompanyCode(companyCode);
        entity.setCourseCode(courseCode);
        entity.setDateForCheck(dateForCheck);
        return entity;
    }
}
