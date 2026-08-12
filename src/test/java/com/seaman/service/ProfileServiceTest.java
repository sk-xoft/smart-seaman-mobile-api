package com.seaman.service;

import com.amazonaws.services.s3.AmazonS3;
import com.seaman.constant.AppStatus;
import com.seaman.entity.CompanyEntity;
import com.seaman.entity.PositionsEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.exception.BusinessException;
import com.seaman.model.request.ProfileRequest;
import com.seaman.model.request.ProfileUserActivateRequest;
import com.seaman.model.response.ProfileResponse;
import com.seaman.model.response.RegisterResponse;
import com.seaman.repository.CompanyRepository;
import com.seaman.repository.PositionRepository;
import com.seaman.repository.UserRepository;
import com.seaman.utils.Base64FileValidator;
import com.seaman.utils.DateUtil;
import com.seaman.utils.FrameworkUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.Period;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Covers the ProfileService methods not already exercised by
 * {@link ProfileMobileNumberUpdateTest} (which covers only the mobile-number-history branch of
 * profileUpdate()).
 */
@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock UserRepository userRepository;
    @Mock CompanyRepository companyRepository;
    @Mock DateUtil dateUtil;
    @Mock HttpServletRequest httpServletRequest;
    @Mock TransactionLogsService transactionLogsService;
    @Mock FrameworkUtils frameworkUtils;
    @Mock AmazonS3 amazonS3;
    @Mock PositionRepository positionRepository;
    @Mock Base64FileValidator base64FileValidator;

    private ProfileService service;
    private UsersEntity user;

    @BeforeEach
    void setUp() {
        service = new ProfileService(userRepository, companyRepository, dateUtil, httpServletRequest,
                transactionLogsService, frameworkUtils, amazonS3, positionRepository, base64FileValidator);
        ReflectionTestUtils.setField(service, "bucketName", "smart-seaman-bucket");
        ReflectionTestUtils.setField(service, "pathImageProfiles", "profiles/images");

        user = new UsersEntity();
        user.setMobileUuid("mobile-user-uuid");
        user.setUsername("crew@example.com");
        user.setEmail("crew@example.com");
        user.setFirstName("Crew");
        user.setLastName("Member");
        user.setSmartSeamanId("00001");
        lenient().when(httpServletRequest.getAttribute(anyString()))
                .thenAnswer(invocation -> "userObject".equals(invocation.getArgument(0)) ? user : null);
        lenient().when(frameworkUtils.toObjectToJson(any())).thenReturn("{}");
    }

    // ---- getProfile

    @Test
    void getProfileFillsCompanyAndPositionAndAgeWhenPresent() {
        user.setDateOfBirth("1990-01-01");
        user.setCompanyCode("COMP001");
        user.setPositionCode("POS001");
        when(httpServletRequest.getHeader(anyString())).thenReturn("EN");
        when(dateUtil.calculateDisplayAge("1990-01-01")).thenReturn(Period.of(35, 0, 0));

        CompanyEntity company = new CompanyEntity();
        company.setCompanyCode("COMP001");
        company.setCompanyNameEn("Company EN");
        company.setCompanyNameTh("Company TH");
        when(companyRepository.findByCode("COMP001")).thenReturn(company);

        PositionsEntity position = new PositionsEntity();
        position.setPositionCode("POS001");
        position.setPositionNameEn("Captain EN");
        position.setPositionNameTh("Captain TH");
        when(positionRepository.findByCode("POS001")).thenReturn(position);

        ProfileResponse response = service.getProfile();

        assertEquals("1990-01-01", response.getDateOfBirth());
        assertEquals("35", response.getAge());
        assertEquals("COMP001", response.getCompanyCode());
        assertEquals("Company EN", response.getCompanyDescription());
        assertEquals("POS001", response.getPositionCode());
        assertEquals("Captain EN", response.getPositionDescription());
        assertEquals("CM", response.getShortName());
    }

    @Test
    void getProfileUsesThaiDescriptionsWhenLanguageNotEn() {
        user.setCompanyCode("COMP001");
        user.setPositionCode("POS001");
        when(httpServletRequest.getHeader(anyString())).thenReturn("TH");

        CompanyEntity company = new CompanyEntity();
        company.setCompanyNameEn("Company EN");
        company.setCompanyNameTh("Company TH");
        when(companyRepository.findByCode("COMP001")).thenReturn(company);

        PositionsEntity position = new PositionsEntity();
        position.setPositionNameEn("Captain EN");
        position.setPositionNameTh("Captain TH");
        when(positionRepository.findByCode("POS001")).thenReturn(position);

        ProfileResponse response = service.getProfile();

        assertEquals("Company TH", response.getCompanyDescription());
        assertEquals("Captain TH", response.getPositionDescription());
    }

    @Test
    void getProfileLeavesCompanyAndPositionEmptyWhenNotSet() {
        user.setCompanyCode(null);
        user.setPositionCode("");

        ProfileResponse response = service.getProfile();

        assertEquals("", response.getCompanyCode());
        assertEquals("", response.getCompanyDescription());
        assertEquals("", response.getPositionCode());
        assertEquals("", response.getPositionDescription());
        assertEquals("", response.getDateOfBirth());
        assertEquals("", response.getAge());
        verifyNoInteractions(companyRepository, positionRepository);
    }

    @Test
    void getProfileSwallowsGenericExceptionAndReturnsPartialResponse() {
        user.setCompanyCode("COMP001");
        when(companyRepository.findByCode("COMP001")).thenThrow(new RuntimeException("db down"));

        ProfileResponse response = service.getProfile();

        // Exception is caught by the generic catch(Exception) block (logged only, not rethrown),
        // so the method returns whatever was populated before the failure rather than throwing.
        assertEquals("Crew", response.getFirstName());
        assertNull(response.getShortName());
    }

    @Test
    void getProfileRethrowsBusinessException() {
        user.setCompanyCode("COMP001");
        when(companyRepository.findByCode("COMP001"))
                .thenThrow(new BusinessException(AppStatus.EXCEPTION_GLOBAL, "boom"));

        assertThrows(BusinessException.class, () -> service.getProfile());
    }

    // ---- profileUpdate (file-upload branch; mobile-number branch covered by ProfileMobileNumberUpdateTest)

    @Test
    void profileUpdateThrowsWhenUserObjectMissing() {
        when(httpServletRequest.getAttribute("userObject")).thenReturn(null);
        ProfileRequest request = profileRequest("0812345678", "N");

        BusinessException ex = assertThrows(BusinessException.class, () -> service.profileUpdate(request));
        assertEquals(AppStatus.USERNAME_IS_NOT_FOUND_SECURITY_CONTEXT, ex.getCode());
    }

    @Test
    void profileUpdateUploadsImageAndUpdatesProfilePictureWhenChangeFileIsY() throws Exception {
        when(userRepository.lockMobileNumber("mobile-user-uuid")).thenReturn("0812345678");
        when(frameworkUtils.generateUUID()).thenReturn("new-picture-uuid");
        when(userRepository.update(any(UsersEntity.class))).thenReturn(true);
        when(userRepository.updateProfilePicture(any(UsersEntity.class))).thenReturn(true);
        ProfileRequest request = profileRequest("0812345678", "Y");
        request.setImageProfile("data:image/png;base64,AAAA");

        RegisterResponse response = service.profileUpdate(request);

        assertEquals("new@example.com", response.getEmail());
        verify(base64FileValidator).validateImage("data:image/png;base64,AAAA", "imageProfile");
        verify(amazonS3).putObject("smart-seaman-bucket", "profiles/images/new-picture-uuid",
                "data:image/png;base64,AAAA");
        verify(userRepository).update(any(UsersEntity.class));
        verify(userRepository).updateProfilePicture(any(UsersEntity.class));
    }

    @Test
    void profileUpdateThrowsWhenDatabaseUpdateFails() {
        when(userRepository.lockMobileNumber("mobile-user-uuid")).thenReturn("0812345678");
        when(userRepository.update(any(UsersEntity.class))).thenReturn(false);
        ProfileRequest request = profileRequest("0812345678", "N");

        BusinessException ex = assertThrows(BusinessException.class, () -> service.profileUpdate(request));
        assertEquals(AppStatus.EXCEPTION_DATABASE, ex.getCode());
    }

    // ---- profileInactive

    @Test
    void profileInactiveSetsStatusDeleted() {
        when(userRepository.updateStatusProfile(any(UsersEntity.class))).thenReturn(true);

        RegisterResponse response = service.profileInactive();

        assertEquals("crew@example.com", response.getEmail());
        assertEquals("D", user.getUserStatus());
        verify(userRepository).updateStatusProfile(user);
    }

    @Test
    void profileInactiveDoesNotThrowWhenUpdateFails() {
        when(userRepository.updateStatusProfile(any(UsersEntity.class))).thenReturn(false);

        // updateStatusProfile()==false only records a different statusCode for the trans log;
        // the response is still returned as if it succeeded.
        RegisterResponse response = service.profileInactive();

        assertEquals("crew@example.com", response.getEmail());
    }

    // ---- profileActive

    @Test
    void profileActiveThrowsWhenEmailNotFound() {
        ProfileUserActivateRequest request = new ProfileUserActivateRequest();
        request.setEmail("missing@example.com");
        when(userRepository.findByEmail("missing@example.com")).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.profileActive(request));
        assertEquals(AppStatus.EMAIL_IS_EXISTING, ex.getCode());
    }

    @Test
    void profileActiveActivatesUserWhenFound() {
        ProfileUserActivateRequest request = new ProfileUserActivateRequest();
        request.setEmail("crew@example.com");
        UsersEntity found = new UsersEntity();
        found.setUsername("crew@example.com");
        found.setEmail("crew@example.com");
        when(userRepository.findByEmail("crew@example.com")).thenReturn(found);
        when(userRepository.updateStatusProfile(found)).thenReturn(true);

        RegisterResponse response = service.profileActive(request);

        assertEquals("A", found.getUserStatus());
        assertEquals("crew@example.com", response.getEmail());
    }

    // ---- getProfileImage

    @Test
    void getProfileImageFetchesFromS3() {
        user.setProfilePicture("picture-uuid");
        when(amazonS3.getObjectAsString("smart-seaman-bucket", "profiles/images/picture-uuid"))
                .thenReturn("base64-image");

        String result = service.getProfileImage();

        assertEquals("base64-image", result);
    }

    // ---- fixtures

    private ProfileRequest profileRequest(String mobileNumber, String isChangeFile) {
        ProfileRequest request = new ProfileRequest();
        request.setFirstName("First");
        request.setLastName("Last");
        request.setPositionCode("POS001");
        request.setEmail("new@example.com");
        request.setMobileNumber(mobileNumber);
        request.setIsChangeFile(isChangeFile);
        return request;
    }
}
