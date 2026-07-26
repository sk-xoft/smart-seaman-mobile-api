package com.seaman.controller;

import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.model.common.SuccessResponse;
import com.seaman.model.request.ProfileRequest;
import com.seaman.model.request.ProfileUserActivateRequest;
import com.seaman.model.response.*;
import com.seaman.service.CourseService;
import com.seaman.service.MasterDataService;
import com.seaman.service.MessageCodeService;
import com.seaman.service.ProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MasterProfileControllerTest {
    private HttpServletRequest request;
    private MessageCodeService messages;

    @BeforeEach
    void setUp() {
        request = mock(HttpServletRequest.class);
        messages = mock(MessageCodeService.class);
        when(request.getAttribute(AppSys.LANGUAGE)).thenReturn("TH");
        when(messages.getMessageDescription(AppStatus.SUCCESS_CODE, "TH")).thenReturn("success");
    }

    @Test
    void masterEndpointsDelegateToServices() {
        MasterDataService master = mock(MasterDataService.class);
        CourseService courses = mock(CourseService.class);
        MasterController controller = new MasterController(master, messages, courses);
        MasterDataResponse masterData = new MasterDataResponse();
        MasterDataDocumentResponse documents = new MasterDataDocumentResponse();
        CourseListResponse courseData = new CourseListResponse();
        List<ThailandAddressResponse> addresses = Collections.singletonList(new ThailandAddressResponse());
        when(master.list()).thenReturn(masterData);
        when(master.masterDataDocuments()).thenReturn(documents);
        when(courses.courses()).thenReturn(courseData);
        when(master.provinces()).thenReturn(addresses);
        when(master.districts(10)).thenReturn(addresses);
        when(master.subdistricts(1001)).thenReturn(addresses);

        assertSame(masterData, controller.master(request).getBody().getData());
        assertSame(documents, controller.masterDocuments(request).getBody().getData());
        assertSame(courseData,
                ((SuccessResponse<?>) controller.masterCourses(request).getBody()).getData());
        assertSame(addresses, controller.provinces(request).getBody().getData());
        assertSame(addresses, controller.districts(request, 10).getBody().getData());
        assertSame(addresses, controller.subdistricts(request, 1001).getBody().getData());
        verify(master).districts(10);
        verify(master).subdistricts(1001);
    }

    @Test
    void profileEndpointsDelegateToService() throws Exception {
        ProfileService profile = mock(ProfileService.class);
        ProfileController controller = new ProfileController(messages, profile);
        ProfileResponse profileData = new ProfileResponse();
        RegisterResponse registerData = new RegisterResponse();
        ProfileRequest profileRequest = new ProfileRequest();
        ProfileUserActivateRequest activateRequest = new ProfileUserActivateRequest();
        when(profile.getProfile()).thenReturn(profileData);
        when(profile.profileUpdate(profileRequest)).thenReturn(registerData);
        when(profile.profileInactive()).thenReturn(registerData);
        when(profile.profileActive(activateRequest)).thenReturn(registerData);
        when(profile.getProfileImage()).thenReturn("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADElEQVR42mP8z8AARQAFAAH+Af9qAAAAAElFTkSuQmCC");

        assertSame(profileData, controller.profile(request).getBody().getData());
        assertSame(registerData, controller.profileUpdate(request, profileRequest).getBody().getData());
        assertSame(registerData, controller.profileInactive(request).getBody().getData());
        assertSame(registerData, controller.profileActive(request, activateRequest).getBody().getData());
        HttpEntity<byte[]> image = controller.getImage();
        assertEquals(MediaType.IMAGE_PNG, image.getHeaders().getContentType());
        assertTrue(image.getBody().length > 0);
    }

    @Test
    void policyAndHomeEndpointsReturnStaticSuccess() {
        PolicyController policy = new PolicyController(messages);
        ResponseEntity<SuccessResponse<String>> policyResponse = policy.activateUser(request);
        assertEquals(AppStatus.SUCCESS_CODE, policyResponse.getBody().getCode());
        assertEquals("success", policyResponse.getBody().getData());

        HomeController home = new HomeController();
        assertEquals("smart-seaman-mobile-api", home.index());
        assertEquals("Success", home.health());
    }
}
