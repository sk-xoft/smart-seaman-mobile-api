package com.seaman.service;

import com.amazonaws.services.s3.AmazonS3;
import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.entity.CompanyEntity;
import com.seaman.entity.PositionsEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.exception.BusinessException;
import com.seaman.exception.CommonException;
import com.seaman.model.request.ProfileRequest;
import com.seaman.model.request.ProfileUserActivateRequest;
import com.seaman.model.response.ProfileResponse;
import com.seaman.model.response.RegisterResponse;
import com.seaman.repository.CompanyRepository;
import com.seaman.repository.PositionRepository;
import com.seaman.repository.UserRepository;
import com.seaman.utils.DateUtil;
import com.seaman.utils.FrameworkUtils;
import lombok.RequiredArgsConstructor;
import net.sf.jmimemagic.MagicException;
import net.sf.jmimemagic.MagicMatchNotFoundException;
import net.sf.jmimemagic.MagicParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.Period;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final DateUtil dateUtil;
    private final HttpServletRequest httpServletRequest;
    private final TransactionLogsService transactionLogsService;
    private final FrameworkUtils frameworkUtils;
    private final AmazonS3 getS3;
    private final PositionRepository positionRepository;

    @Value("${object.store.bucket}")
    private String bucketName;

    @Value("${object.store.path.profiles.images}")
    private String pathImageProfiles;

    public ProfileResponse getProfile() {

        String lang = httpServletRequest.getHeader(AppSys.HEADER_ACCEPT_LANGUAGE);

        UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");

        ProfileResponse profileResponse = new ProfileResponse();
        try {
            profileResponse.setFirstName(usersEntity.getFirstName());
            profileResponse.setLastName(usersEntity.getLastName());

            if (null != usersEntity.getDateOfBirth() && !"".equals(usersEntity.getDateOfBirth())) {
//                Date dateOfBirth = dateUtil.parseStringToDate(usersEntity.getDateOfBirth(), DateUtil.YEAR_MONTH_DATE);
//                String dateOfBirthStr =  dateUtil.formatDateToString(dateOfBirth, DateUtil.DATE_OF_BIRTH);
                profileResponse.setDateOfBirth(usersEntity.getDateOfBirth());

                Period age = dateUtil.calculateDisplayAge(usersEntity.getDateOfBirth());
                profileResponse.setAge(String.valueOf(age.getYears()));
            } else {
                profileResponse.setDateOfBirth("");
                profileResponse.setAge("");
            }

            profileResponse.setMobile(usersEntity.getMobileNumber());
            profileResponse.setEmail(usersEntity.getEmail());

            if(null != usersEntity.getCompanyCode() && !"".equals(usersEntity.getCompanyCode())) {
                CompanyEntity companyEntity = companyRepository.findByCode(usersEntity.getCompanyCode());
                profileResponse.setCompanyCode(companyEntity.getCompanyCode());
                profileResponse.setCompanyDescription(AppSys.LANG_EN.equals(lang) ? companyEntity.getCompanyNameEn() : companyEntity.getCompanyNameTh());
            } else {
                profileResponse.setCompanyCode("");
                profileResponse.setCompanyDescription("");
            }

            if(null != usersEntity.getPositionCode() && !"".equals(usersEntity.getPositionCode())){
                PositionsEntity positionsEntity = positionRepository.findByCode(usersEntity.getPositionCode());
                profileResponse.setPositionCode(positionsEntity.getPositionCode());
                profileResponse.setPositionDescription(AppSys.LANG_EN.equals(lang) ? positionsEntity.getPositionNameEn() : positionsEntity.getPositionNameTh());
            } else {
                profileResponse.setPositionCode("");
                profileResponse.setPositionDescription("");
            }

            profileResponse.setSmartSeamanId(usersEntity.getSmartSeamanId());

            // Short name
            String var1 = usersEntity.getFirstName().substring(0, 1).toUpperCase();
            String var2 = usersEntity.getLastName().substring(0, 1).toUpperCase();
            profileResponse.setShortName(var1 + var2);

        } catch (BusinessException be) {
            log.error("{} error -> {}", "Get Profile", be);
            throw be;
        } catch (Exception ex) {
            log.error("{}", String.valueOf(ex));
        }

        return profileResponse;
    }

    public RegisterResponse profileUpdate(ProfileRequest request) throws MagicMatchNotFoundException, MagicException, MagicParseException {

        RegisterResponse response = new RegisterResponse();

        boolean isStatusUpdate = false;

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "USER PROFILE CHANGED";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            UsersEntity entity = new UsersEntity();
            entity.setMobileUuid(usersEntity.getMobileUuid());
            entity.setUsername(request.getEmail());
            entity.setEmail(request.getEmail());
            entity.setFirstName(request.getFirstName());
            entity.setLastName(request.getLastName());
            entity.setMobileNumber(request.getMobileNumber());
            entity.setCompanyCode(request.getCompanyCode());
            entity.setPositionCode(request.getPositionCode());
            entity.setUpdateBy(request.getEmail());
            entity.setUpdateDate(new Date());
            entity.setDateOfBirth(request.getDateOfBirth());

            if (request.getIsChangeFile().equals("N")) {
                isStatusUpdate = userRepository.update(entity);
            } else {

                if (!"".equals(request.getImageProfile()) || null != request.getImageProfile()) {
                    String fileNameProfilePic = frameworkUtils.generateUUID();

                    String keyName = pathImageProfiles + "/" + fileNameProfilePic;
                    getS3.putObject(bucketName, keyName, request.getImageProfile());

                    // Update data info
                    isStatusUpdate = userRepository.update(entity);

                    // Update profile image
                    entity.setProfilePicture(fileNameProfilePic);
                    isStatusUpdate = userRepository.updateProfilePicture(entity);

                    log.info("put object profile pic. {} is success.", keyName);
                } else {
                    log.info("Not have send file 'profile pic'.");
                }
            }

            if (!isStatusUpdate) {
                // Case not update user profile.
                statusCode = AppStatus.USERNAME_IS_NOT_FOUND_SECURITY_CONTEXT;
            }

            response.setEmail(request.getEmail());
            response.setUsername(request.getEmail());

        } catch (CommonException ce) {
            log.error("{}", ce.getMessage());
            statusCode = ce.getCode();
            throw ce;
        } catch (Exception ex) {
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            log.error("{} error -> {}", serviceName, ex);
            throw ex;
        } finally {
            String resJson = frameworkUtils.toObjectToJson(response);
            transactionLogsService.update(transId, resJson, statusCode, username);
        }
        return response;
    }

    public RegisterResponse profileInactive() {

        RegisterResponse response = new RegisterResponse();

        boolean isStatusUpdate = false;

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "DELETE PROFILE";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            usersEntity.setUserStatus("D"); // Set status user is delete   D =  DELETE
            usersEntity.setUpdateBy(username);

            isStatusUpdate = userRepository.updateStatusProfile(usersEntity);

            if (!isStatusUpdate) {
                // Case not update user profile.
                log.info("User is can not inactive.");
                statusCode = AppStatus.USERNAME_IS_NOT_FOUND_SECURITY_CONTEXT;
            }

            response.setEmail(usersEntity.getEmail());
            response.setUsername(usersEntity.getUsername());
            log.info("Users {} is inactive is success.");

        } catch (CommonException ce) {
            log.error("{}", ce.getMessage());
            statusCode = ce.getCode();
            throw ce;
        } catch (Exception ex) {
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            log.error("{} error -> {}", serviceName, ex);
            throw ex;
        } finally {
            String resJson = frameworkUtils.toObjectToJson(response);
            transactionLogsService.update(transId, resJson, statusCode, username);
        }
        return response;
    }

    public RegisterResponse profileActive(ProfileUserActivateRequest request) {

        RegisterResponse response = new RegisterResponse();

        boolean isStatusUpdate = false;

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "ACTIVE PROFILE";
        String username = "";

        try {

            UsersEntity usersEntity = userRepository.findByEmail(request.getEmail());
            if(null == usersEntity) {
                throw new BusinessException(AppStatus.EMAIL_IS_EXISTING, "");
            }

            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            /** Case, When user is destroyed account, After have login  again. User can activate status user. **/
            usersEntity.setUserStatus("A");
            usersEntity.setUpdateBy(username);

            isStatusUpdate = userRepository.updateStatusProfile(usersEntity);

            if (!isStatusUpdate) {
                // Case not update user profile.
                log.info("User is can not activate.");
                statusCode = AppStatus.USERNAME_IS_NOT_FOUND_SECURITY_CONTEXT;
            }

            response.setEmail(usersEntity.getEmail());
            response.setUsername(usersEntity.getUsername());
            log.info("Users {} is active is success.");

        } catch (CommonException ce) {
            log.error("{}", ce.getMessage());
            statusCode = ce.getCode();
            throw ce;
        } catch (Exception ex) {
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            log.error("{} error -> {}", serviceName, ex);
            throw ex;
        } finally {
            String resJson = frameworkUtils.toObjectToJson(response);
            transactionLogsService.update(transId, resJson, statusCode, username);
        }
        return response;
    }


    public String getProfileImage() {

        String imageBase64 = "";

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "PROFILE USER";
        String username = "";

        try {
            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            String keyName = pathImageProfiles + "/" + usersEntity.getProfilePicture();
            imageBase64 = getS3.getObjectAsString(bucketName, keyName);

        } catch (CommonException ce) {
            log.error("{}", ce.getMessage());
            statusCode = ce.getCode();
            throw ce;
        } catch (Exception ex) {
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            log.error("{} error -> {}", serviceName, ex);
            throw ex;
        } finally {
            String resJson = "{\"username\" : \"" + username + "\"}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return imageBase64;
    }
}
