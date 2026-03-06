package com.seaman.service;

import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.entity.*;
import com.seaman.exception.BusinessException;
import com.seaman.exception.CommonException;
import com.seaman.model.request.*;
import com.seaman.model.response.LoginResponse;
import com.seaman.model.response.RefreshTokenResponse;
import com.seaman.model.response.RegisterResponse;
import com.seaman.repository.*;
import com.seaman.utils.DateUtil;
import com.seaman.utils.FrameworkUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final HttpServletRequest httpServletRequest;

    private final JwtTokenService jwtTokenUtil;

    private final FrameworkUtils frameworkUtils;

    private final UserRepository userRepository;

    private final SessionRepository sessionRepository;

    private final DocumentRepository documentRepository;

    private final CertificateRepository certificateRepository;

    private final TransactionLogsService transactionLogsService;

    private final PasswordEncoder passwordEncoder;

    private final ForgotPasswordRepository forgotPasswordRepository;

    private final DateUtil dateUtil;

    private final EmailService emailService;

    @Value("${register.confirm.url}")
    private String linkConfirmRegister;

    @Value("${forgot.confirm.url}")
    private String linkConfirmForgot;

    public LoginResponse login(LoginRequest loginRequest) {

        // Set default status.
        String statusCode  =  AppStatus.SUCCESS_CODE;

        LoginResponse response = new LoginResponse();
        String correlationId = httpServletRequest.getHeader(AppSys.HEADER_CORRELATION_ID);

        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "LOGIN";
        String username = "";

        try {

            // Store trans
            transactionLogsService.insert(transId, bodyReqJson, serviceName, loginRequest.getUsername());

            // -- 1. find username in table, And check password
            Optional<UsersEntity> usersEntityOptional = Optional.ofNullable(userRepository.findByUsername(loginRequest.getUsername()));

            if (usersEntityOptional.isEmpty()) {
                throw new BusinessException(AppStatus.EXCEPTION_USERNAME_INCORRECT, "");
            }

            UsersEntity usersEntity = usersEntityOptional.get();
            if (!this.matchPassword(loginRequest.getPassword(), usersEntity.getPassword())) {
                throw new BusinessException(AppStatus.EXCEPTION_USERNAME_PASSWORD_INCORRECT, "");
            }

            log.info("User is status -> {}.", usersEntity.getUserStatus());

            if("D".equals(usersEntity.getUserStatus())) {
                throw new BusinessException(AppStatus.USER_IS_INACTIVATED, "");
            } else if(!"A".equals(usersEntity.getUserStatus())) {
                throw new BusinessException(AppStatus.SECURITY_NOT_FOUND_USERNAME, "");
            }

            // Set username for insert log.
            username = loginRequest.getUsername();

            // -- 2. Generate JWT
            // Ref : https://www.rfc-editor.org/rfc/rfc7519#section-4.1
            String clientSessionId = frameworkUtils.generateUUID();
            Map<String, Object> claims = new HashMap<>();
            claims.put(AppSys.CLAIMS_ISSUER, AppSys.APPLICATION_NAME);
            claims.put(AppSys.CLAIMS_JTI, clientSessionId);
            claims.put(AppSys.CLAIMS_SUBJECT, loginRequest.getUsername());

            // Create JWT TOKEN
            String jwtToken = jwtTokenUtil.generateToken(claims, loginRequest.getUsername());

            // Store table session
            SessionEntity sessionEntity = new SessionEntity();
            sessionEntity.setClientSessionId(clientSessionId);
            sessionEntity.setToken(jwtToken);
            sessionEntity.setDeviceModel("MOBILE"); // must use form header request.
            sessionEntity.setUserId(usersEntity.getMobileUuid());
            sessionEntity.setCreateBy(loginRequest.getUsername());
            sessionEntity.setLoginTime(new Date());
            sessionEntity.setCreateDate(new Date());
            sessionEntity.setUpdateBy(loginRequest.getUsername());
            sessionEntity.setUpdateDate(new Date());
            sessionEntity.setIsOnline("YES");
            sessionEntity.setCorrelationId(correlationId);

            // Set session object for transaction logs.
            httpServletRequest.setAttribute("sessionObject", sessionEntity);
            sessionRepository.insert(sessionEntity);


            // Mark response
            response.setToken(jwtToken);
            response.setRefToken(clientSessionId);
            response.setUsername(loginRequest.getUsername());
            response.setLastLoginDateTime(dateUtil.convertTime(new Date().getTime()));

            log.info("{}", "Process login is success.");
        } catch (CommonException ce){
            statusCode = ce.getCode();
            throw ce;
        } catch (Exception ex){
            log.error("Login Exception {}", ex.getMessage());
            statusCode = AppStatus.EXCEPTION_TECHNICAL;
            throw ex;
        } finally {
            String resJson = frameworkUtils.toObjectToJson(response);
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return response;
    }

    public RegisterResponse register(RegisterRequest request){

        String statusCode  =  AppStatus.SUCCESS_CODE;
        RegisterResponse response  = new RegisterResponse();

        String userUUID = frameworkUtils.generateUUID();
        String smartSeaManId = userRepository.countMax().toString();

        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "REGISTER";
        String createBy = request.getEmail();

        try {

            // Store trans
            transactionLogsService.insert(transId, bodyReqJson, serviceName, createBy);

            Optional<UsersEntity> isEmail = Optional.ofNullable(userRepository.findByEmail(request.getEmail()));
            if (isEmail.isEmpty()) {
                UsersEntity entity = new UsersEntity();
                entity.setUsername(request.getEmail());
                entity.setEmail(request.getEmail());
                entity.setPassword(passwordEncoder.encode(request.getPassword()));
                entity.setMobileUuid(userUUID);
                entity.setFirstName(request.getFirstName());
                entity.setLastName(request.getLastName());
                entity.setMobileNumber(request.getMobileNumber());
                entity.setCompanyCode(request.getCompanyCode());
                entity.setPositionCode(request.getPositionCode());
                entity.setProfilePicture("default");
                entity.setSmartSeamanId(frameworkUtils.padLeftZeros(smartSeaManId, 5));
                entity.setUserStatus("I");
                entity.setCreateBy(request.getEmail());
                entity.setCreateDate(new Date());
                entity.setUpdateBy(request.getEmail());
                entity.setUpdateDate(new Date());
                entity.setDateOfBirth(request.getDateOfBirth());
                // Short name
                String var1  =  request.getFirstName().substring(0,1).toUpperCase();
                String var2  =  request.getLastName().substring(0,1).toUpperCase();
                entity.setDisplayName(var1+var2);
                entity.setDisplayType("NAME");
                boolean isInsertUser = userRepository.insert(entity);

                if(isInsertUser) {

                    // Insert Default document
                    List<DocumentEntity> documentDefault = documentRepository.findDefault();

                    for (DocumentEntity item : documentDefault) {

                        CertificateEntity certificateEntity = new CertificateEntity();
                        certificateEntity.setCertMobileUuid(userUUID);
                        certificateEntity.setCertDocumentCode(item.getDocumentCode());
                        certificateEntity.setCertStartDate(null);
                        certificateEntity.setCertEndDate(null);
                        certificateEntity.setCertStatus("N");
                        certificateEntity.setCertFile(null);
                        certificateEntity.setCreateDate(new Date());
                        certificateEntity.setCreateBy(request.getEmail());
                        certificateEntity.setUpdateDate(new Date());
                        certificateEntity.setUpdateBy(request.getEmail());
                        certificateEntity.setOriginalFileName(null);

                        // Insert cert is success
                        certificateRepository.insert(certificateEntity);
                    }

                    // Step send e-mail.
                    String fullName = request.getFirstName() + " " + request.getLastName();
                    String toEmail = request.getEmail();
                    emailService.sendEmailRegister(fullName, toEmail, linkConfirmRegister, userUUID);

                    log.info("Is register is success. And send email is success.");
                } else {
                    throw new BusinessException(AppStatus.CANNOT_REGISTER, "Can not insert user.");
                }

                // Set Response
                response.setUsername(request.getEmail());
                response.setEmail(request.getEmail());
            } else {
                throw new BusinessException(AppStatus.EMAIL_IS_REGISTER, "");
            }

        } catch (CommonException ce){
            statusCode = ce.getCode();
            throw  ce;
        } catch(Exception ex){
            statusCode = AppStatus.EXCEPTION_TECHNICAL;
            log.error("Register Exception {}", ex.getMessage());
            throw ex;
        } finally {
            String resJson = frameworkUtils.toObjectToJson(response);
            transactionLogsService.update(transId, resJson, statusCode, request.getEmail());
        }

        return response;
    }

    private boolean matchPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public RefreshTokenResponse refreshToken(String refToken) {

        String statusCode  =  AppStatus.SUCCESS_CODE;
        RefreshTokenResponse response  = new RefreshTokenResponse();

        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "REFRESH TOKEN";
        String username = "";

        try {
            // set ref_token
            httpServletRequest.setAttribute("clientSessionId", refToken);

            // Store trans
            transactionLogsService.insert(transId, bodyReqJson, serviceName, username);

            // Optional<String> opt = SecurityUtils.getCurrentUserId();
//            if (opt.isEmpty()) {
//                throw new BusinessException(AppStatus.SECURITY_NOT_FOUND_USERNAME, null);
//            }
            // userId = opt.get();
            SessionEntity sessionEntity =  sessionRepository.findByClientSessionId(refToken);
            if (sessionEntity == null) {
                log.error("{}", "Ref-token is not found.");
                throw new BusinessException(AppStatus.INVALID_UUID, null);
            }

            UsersEntity usersEntityOptional  = userRepository.findByUserUID(sessionEntity.getUserId());
            username = usersEntityOptional.getUsername();
            if (usersEntityOptional == null) {
                log.error("{}", "Username is not found.");
                throw new BusinessException(AppStatus.SECURITY_NOT_FOUND_USERNAME, null);
            }

            // -- 2. Generate JWT
            // Ref : https://www.rfc-editor.org/rfc/rfc7519#section-4.1

            Map<String, Object> claims = new HashMap<>();
            claims.put(AppSys.CLAIMS_ISSUER, AppSys.APPLICATION_NAME);

            // Set client
            claims.put(AppSys.CLAIMS_JTI, refToken);

            claims.put(AppSys.CLAIMS_SUBJECT, username);

            // Create JWT TOKEN
            String jwtToken = jwtTokenUtil.generateToken(claims, username);

            // Update toke
            sessionEntity.setToken(jwtToken);
            sessionRepository.update(sessionEntity);
            
            response.setToken(jwtToken);

        } catch (CommonException ce){
            statusCode = ce.getCode();
            throw  ce;
        } catch(Exception ex){
            statusCode = AppStatus.EXCEPTION_TECHNICAL;
            log.error("Refresh Exception {}", ex.getMessage());
            throw ex;
        } finally {
            String resJson = frameworkUtils.toObjectToJson(response);
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return response;

    }

    public RegisterResponse changePassword(HttpServletRequest httpServletRequest, ChangePasswordRequest request) {

        String statusCode  =  AppStatus.SUCCESS_CODE;
        RegisterResponse response  = new RegisterResponse();

        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "PASSWORD CHANGED";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Store trans
            transactionLogsService.insert(transId, bodyReqJson, serviceName, username);

            // Check password is match.
            if(request.getConfirmPassword().equals(request.getNewPassword())) {

                // Find user check password.

                // -- 1. find username in table, And check password
                Optional<UsersEntity> usersEntityOptional = Optional.ofNullable(userRepository.findByUsername(username));

                if (usersEntityOptional.isEmpty()) {
                    throw new BusinessException(AppStatus.EXCEPTION_USERNAME_PASSWORD_INCORRECT, "");
                }

                UsersEntity usersEntityDB = usersEntityOptional.get();
                if (!this.matchPassword(request.getOldPassword(), usersEntityDB.getPassword())) {
                    throw new BusinessException(AppStatus.EXCEPTION_USERNAME_PASSWORD_INCORRECT, "");
                }

                usersEntityDB.setPassword(passwordEncoder.encode(request.getConfirmPassword()));

                userRepository.changePassword(usersEntityDB);

                // Set Response
                response.setUsername(usersEntityDB.getEmail());
                response.setEmail(usersEntityDB.getEmail());

            } else {
                throw new BusinessException(AppStatus.PASSWORD_IS_MATCH, "");
            }


        } catch (CommonException ce){
            statusCode = ce.getCode();
            throw  ce;
        } catch(Exception ex){
            statusCode = AppStatus.EXCEPTION_TECHNICAL;
            log.error("Change password -> {}", ex.getMessage());
            throw ex;
        } finally {
            String resJson = frameworkUtils.toObjectToJson(response);
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return response;
    }

    public Object activateUser(HttpServletRequest httpServletRequest, String uid) {

        String statusCode  =  AppStatus.SUCCESS_CODE;
        RegisterResponse response  = new RegisterResponse();

        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "ACTIVATED USER";
        String username = "";

        try {

            UsersEntity usersEntity = userRepository.findByUserUID(uid);
            if(null == usersEntity) {
                // Case user not found by uid.
                throw new BusinessException(AppStatus.DATA_NOT_FOUND, "");
            }

            // Check user is activated
            if("A".equals(usersEntity.getUserStatus())) {
                throw new BusinessException(AppStatus.USER_IS_ACTIVATED, "");
            }

            username = usersEntity.getUsername();

            // Store trans
            transactionLogsService.insert(transId, bodyReqJson, serviceName, username);

            usersEntity.setUserStatus("A");
            userRepository.updateStatus(usersEntity);

            response.setUsername(usersEntity.getUsername());
            response.setEmail(usersEntity.getEmail());

        } catch (CommonException ce){
            statusCode = ce.getCode();
            throw  ce;
        } catch(Exception ex){
            statusCode = AppStatus.EXCEPTION_TECHNICAL;
            log.error("Change password -> {}", ex.getMessage());
            throw ex;
        } finally {
            String resJson = frameworkUtils.toObjectToJson(response);
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return response;
    }

    public Object resetPassword(HttpServletRequest httpServletRequest, ResetPasswordRequest request) {

        String statusCode  =  AppStatus.SUCCESS_CODE;
        RegisterResponse response  = new RegisterResponse();

        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "PASSWORD RESETTING";
        String username = "";

        try {

            UsersEntity usersEntity = userRepository.findByEmail(request.getEmail());
            if(null == usersEntity) {
                // Case user not found by uid.
                throw new BusinessException(AppStatus.DATA_NOT_FOUND, "");
            }

            username = usersEntity.getUsername();

            // Store trans
            transactionLogsService.insert(transId, bodyReqJson, serviceName, username);

            // Setup Response
            response.setUsername(usersEntity.getUsername());
            response.setEmail(usersEntity.getEmail());

            // Step send e-mail.
            String fullName = usersEntity.getFirstName() + " " + usersEntity.getLastName();
            String toEmail = request.getEmail();
            emailService.sendEmailForgotPassword(fullName, toEmail, linkConfirmForgot, usersEntity.getMobileUuid());

            // Store Trans Forgot password.
            forgotPasswordRepository.insert(usersEntity.getMobileUuid());

        } catch (CommonException ce){
            statusCode = ce.getCode();
            throw  ce;
        } catch(Exception ex){
            statusCode = AppStatus.EXCEPTION_TECHNICAL;
            log.error("Change password -> {}", ex.getMessage());
            throw ex;
        } finally {
            String resJson = frameworkUtils.toObjectToJson(response);
            transactionLogsService.update(transId, resJson, statusCode, username);
        }
        return response;
    }

    public Object forgotPassword(HttpServletRequest httpServletRequest, ForgotPasswordRequest request) {

        String statusCode  =  AppStatus.SUCCESS_CODE;
        RegisterResponse response  = new RegisterResponse();

        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "FORGOT PASSWORD";
        String username = "";

        try {

            UsersEntity usersEntity = userRepository.findByUserUID(request.getUid());

            if(null == usersEntity) {
                // Case user not found by uid.
                throw new BusinessException(AppStatus.DATA_NOT_FOUND, "");
            }

            username = usersEntity.getUsername();

            // Store trans
            transactionLogsService.insert(transId, bodyReqJson, serviceName, username);

            // Check password is match.
            if(request.getConfirmPassword().equals(request.getPassword())) {

                // Find user check password.
                // -- 1. find username in table, And check password
                Optional<UsersEntity> usersEntityOptional = Optional.ofNullable(userRepository.findByUsername(username));

                if (usersEntityOptional.isEmpty()) {
                    throw new BusinessException(AppStatus.EXCEPTION_USERNAME_PASSWORD_INCORRECT, "");
                }

                usersEntity.setPassword(passwordEncoder.encode(request.getConfirmPassword()));
                userRepository.changePassword(usersEntity);

                // Set Response
                response.setUsername(usersEntity.getEmail());
                response.setEmail(usersEntity.getEmail());

            } else {
                throw new BusinessException(AppStatus.PASSWORD_IS_MATCH, "");
            }

        } catch (CommonException ce){
            statusCode = ce.getCode();
            throw  ce;
        } catch(Exception ex){
            statusCode = AppStatus.EXCEPTION_TECHNICAL;
            log.error("Forgot password -> {}", ex.getMessage());
            throw ex;
        } finally {
            String resJson = frameworkUtils.toObjectToJson(response);
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return response;
    }
}
