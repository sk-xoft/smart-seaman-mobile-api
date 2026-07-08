package com.seaman.service;

import com.amazonaws.services.s3.AmazonS3;
import com.seaman.entity.UsersEntity;
import com.seaman.model.request.ProfileRequest;
import com.seaman.repository.CompanyRepository;
import com.seaman.repository.PositionRepository;
import com.seaman.repository.UserRepository;
import com.seaman.utils.DateUtil;
import com.seaman.utils.FrameworkUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileMobileNumberUpdateTest {

    @Mock UserRepository userRepository;
    @Mock CompanyRepository companyRepository;
    @Mock DateUtil dateUtil;
    @Mock HttpServletRequest httpServletRequest;
    @Mock TransactionLogsService transactionLogsService;
    @Mock FrameworkUtils frameworkUtils;
    @Mock AmazonS3 amazonS3;
    @Mock PositionRepository positionRepository;

    private ProfileService service;
    private UsersEntity authenticatedUser;

    @BeforeEach
    void setUp() {
        service = new ProfileService(userRepository, companyRepository, dateUtil,
                httpServletRequest, transactionLogsService, frameworkUtils, amazonS3,
                positionRepository);
        authenticatedUser = new UsersEntity();
        authenticatedUser.setMobileUuid("mobile-user-uuid");
        authenticatedUser.setUsername("current@example.com");
        when(httpServletRequest.getAttribute(anyString())).thenAnswer(invocation ->
                "userObject".equals(invocation.getArgument(0)) ? authenticatedUser : null);
        when(userRepository.update(any(UsersEntity.class))).thenReturn(true);
    }

    @Test
    void changedNumberWritesHistoryBeforeUpdatingProfile() throws Exception {
        when(userRepository.lockMobileNumber("mobile-user-uuid")).thenReturn("0811111111");

        service.profileUpdate(request("0822222222"));

        org.mockito.InOrder order = org.mockito.Mockito.inOrder(userRepository);
        order.verify(userRepository).lockMobileNumber("mobile-user-uuid");
        order.verify(userRepository).insertMobileNumberHistory(
                "mobile-user-uuid", "0811111111", "0822222222", "current@example.com");
        order.verify(userRepository).update(any(UsersEntity.class));
    }

    @Test
    void unchangedNumberDoesNotWriteHistory() throws Exception {
        when(userRepository.lockMobileNumber("mobile-user-uuid")).thenReturn("0811111111");

        service.profileUpdate(request("0811111111"));

        verify(userRepository, never()).insertMobileNumberHistory(any(), any(), any(), any());
        verify(userRepository).update(any(UsersEntity.class));
    }

    @Test
    void nullOldNumberIsRecorded() throws Exception {
        when(userRepository.lockMobileNumber("mobile-user-uuid")).thenReturn(null);

        service.profileUpdate(request("0811111111"));

        verify(userRepository).insertMobileNumberHistory(
                "mobile-user-uuid", null, "0811111111", "current@example.com");
    }

    private ProfileRequest request(String mobileNumber) {
        ProfileRequest request = new ProfileRequest();
        request.setFirstName("First");
        request.setLastName("Last");
        request.setPositionCode("POS001");
        request.setEmail("new@example.com");
        request.setMobileNumber(mobileNumber);
        request.setIsChangeFile("N");
        return request;
    }
}
