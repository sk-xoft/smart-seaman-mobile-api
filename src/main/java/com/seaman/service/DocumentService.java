package com.seaman.service;

import com.amazonaws.services.s3.AmazonS3;
import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.entity.CertificateEntity;
import com.seaman.entity.DocumentEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.exception.BusinessException;
import com.seaman.exception.CommonException;
import com.seaman.model.request.DocumentCreateRequest;
import com.seaman.model.request.DocumentUpdateRequest;
import com.seaman.model.response.DocumentCreateResponse;
import com.seaman.model.response.DocumentUpdateResponse;
import com.seaman.model.response.PageDocumentResponse;
import com.seaman.repository.CertificateRepository;
import com.seaman.repository.DocumentRepository;
import com.seaman.utils.DateUtil;
import com.seaman.utils.FrameworkUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletRequest;
import java.time.Period;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final HttpServletRequest httpServletRequest;
    private final DocumentRepository documentRepository;
    private final CertificateRepository certificateRepository;
    private final DateUtil dateUtil;
    private final AmazonS3 getS3;
    private final FrameworkUtils frameworkUtils;

    private final TransactionLogsService transactionLogsService;

    @Value("${object.store.bucket}")
    private String bucketName;

    @Value("${object.store.path.template}")
    private String storePathTemplate;

    public PageDocumentResponse pageDocument(int offSet, String documentType) {

        PageDocumentResponse response = new PageDocumentResponse();

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String supType = documentType;
        String serviceName =  "";

        if("cot".equalsIgnoreCase(supType.toUpperCase())){
            serviceName = "CERTIFICATE OF TRAINING";
        } else  {
            serviceName = "DOCUMENTS";
        }
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            List<DocumentEntity> documentEntityList = documentRepository.findByPage(usersEntity.getMobileUuid(), offSet, documentType);
            int itemTotal = documentRepository.countByPageByUserUid(usersEntity.getMobileUuid(), documentType);
            response.setItemTotal(itemTotal);

            // Set lasted is true
            if(documentEntityList.size() < offSet) {
                response.setLast(true);
            }

            if(documentEntityList.size() >= itemTotal) {
                response.setLast(true);
            }

            List<DocumentEntity> documentEntityResultList = new ArrayList<>();
            for (DocumentEntity entity: documentEntityList){

                if(null == entity.getCertStartDate() && null == entity.getCertEndDate()) {
                    /**
                     * this case customer new register.
                     */
                    entity.setDisYear("");
                    entity.setDisMonth("");
                    entity.setDisDay("");

                } else {
                    if (null != entity.getCertEndDate()) {
                        Period certPeriod = dateUtil.calculateDisplayDateCertRemain(entity.getCertEndDate());
                        entity.setDisYear(String.valueOf(certPeriod.getYears()));
                        entity.setDisMonth(String.valueOf(certPeriod.getMonths()));
                        entity.setDisDay(String.valueOf(certPeriod.getDays()));
                    } else {
                        entity.setDisYear("-");
                        entity.setDisMonth("-");
                        entity.setDisDay("-");
                    }
                }

                if(entity.getCertStartDate() != null) {
                    entity.setCertStartDate(dateUtil.formatDateToStr(entity.getCertStartDate(), DateUtil.YEAR_MONTH_DATE));
                }

                if(entity.getCertEndDate() != null) {
                    entity.setCertEndDate(dateUtil.formatDateToStr(entity.getCertEndDate(), DateUtil.YEAR_MONTH_DATE));
                }

                // Add Item to list object.
                documentEntityResultList.add(entity);
            }

            response.setItems(documentEntityResultList);

        } catch (CommonException ce){
            statusCode = ce.getCode();
            throw  ce;
        } catch(Exception ex){
            log.error("Page Document Exception {}", ex.getMessage());
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());
        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }
        return response;
    }

    public PageDocumentResponse closeToExpiration(int offSet) {

        PageDocumentResponse response = new PageDocumentResponse();

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "DOCUMENTS EXPIRATION CHECKED";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");

            // Store trans
            username = usersEntity.getUsername();
            transactionLogsService.insert(transId, bodyReqJson, serviceName, username);

            List<DocumentEntity> documentEntityList = documentRepository.findCloseToExpiration(usersEntity.getMobileUuid(), offSet);
            int itemTotal = documentRepository.countByPageByUserUidCloseToExpiration(usersEntity.getMobileUuid());
            response.setItemTotal(itemTotal);

            // Set lasted is true
            if(documentEntityList.size() <= offSet) {
                response.setLast(true);
            }

            if(documentEntityList.size() >= itemTotal) {
                response.setLast(true);
            }

            List<DocumentEntity> documentEntityResultList = new ArrayList<>();
            for (DocumentEntity entity: documentEntityList){

                if(null != entity.getCertEndDate()) {
                    Period certPeriod = dateUtil.calculateDisplayDateCertRemain(entity.getCertEndDate());
                    entity.setDisYear(String.valueOf(certPeriod.getYears()));
                    entity.setDisMonth(String.valueOf(certPeriod.getMonths()));
                    entity.setDisDay(String.valueOf(certPeriod.getDays()));
                } else {
                    entity.setDisYear("-");
                    entity.setDisMonth("-");
                    entity.setDisDay("-");
                }

                if(entity.getCertStartDate() != null) {
                    entity.setCertStartDate(dateUtil.formatDateToStr(entity.getCertStartDate(), DateUtil.YEAR_MONTH_DATE));
                }

                if(entity.getCertEndDate() != null) {
                    entity.setCertEndDate(dateUtil.formatDateToStr(entity.getCertEndDate(), DateUtil.YEAR_MONTH_DATE));
                }

                // Add Item to list object.
                documentEntityResultList.add(entity);
            }

            response.setItems(documentEntityResultList);

        } catch (CommonException ce){
            statusCode = ce.getCode();
            log.error("{} error -> {}", serviceName, ce);
            throw  ce;
        } catch(Exception ex){
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            log.error("{} error -> {}", serviceName, ex);
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());
        } finally {
            String resJson = "{}";
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return response;
    }

    public DocumentCreateResponse documentCreate(DocumentCreateRequest request) {

        DocumentCreateResponse response = new DocumentCreateResponse();

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "CERT_CREATE";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");

            // Store trans
            username = usersEntity.getUsername();
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            String newFileName = frameworkUtils.generateUUID();

            List<CertificateEntity> certificateEntityList = certificateRepository.findByUsersAndCertCodeList(usersEntity.getMobileUuid(),  request.getDocumentCode());
            if(!certificateEntityList.isEmpty()) {
                throw new BusinessException(AppStatus.DATA_IS_EXISTING, request.getDocumentCode());
            }

            // Insert table
            CertificateEntity entity  = new CertificateEntity();
            entity.setCertMobileUuid(usersEntity.getMobileUuid());
            entity.setCertDocumentCode(request.getDocumentCode());
            entity.setCertStartDate(request.getCertStartDate());
            entity.setCertEndDate(request.getCertEndDate());
            entity.setCertStatus("A");
            entity.setCertFile(newFileName);
            entity.setCreateDate(new Date());
            entity.setCreateBy(usersEntity.getUsername());
            entity.setUpdateDate(new Date());
            entity.setUpdateBy(usersEntity.getUsername());
            entity.setOriginalFileName(request.getFileCertName());

            // Insert cert is success
            if(certificateRepository.insert(entity)) {

                // Upload file to S3.
                if(!"".equals(request.getFileCert()) || null != request.getFileCert()) {
                    String keyName = String.format(storePathTemplate, usersEntity.getMobileUuid(), newFileName);
                    getS3.putObject(bucketName, keyName, request.getFileCert());
                    log.info("put object {} is success.", keyName);
                } else {
                    log.info("Not have send file 'Cert'.");
                }

                // Set response
                response.setDocumentCode(request.getDocumentCode());
                response.setCertStartDate(request.getCertStartDate());
                response.setCertEndDateType(request.getCertEndDateType());
                response.setCertEndDate(request.getCertEndDate());
                response.setFileCertName(request.getFileCertName());

                log.info("Create Cert is success.");
            }

        } catch (CommonException ce){
            statusCode = ce.getCode();
            throw  ce;
        } catch(Exception ex){
            log.error("Document create Exception {}", ex.getMessage());
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());
        }  finally {
            String resJson = frameworkUtils.toObjectToJson(response);
            transactionLogsService.update(transId, resJson, statusCode, username);
        }
        return response;
    }

    public DocumentUpdateResponse documentUpdate(DocumentUpdateRequest request) {

        DocumentUpdateResponse response = new DocumentUpdateResponse();

        boolean isStatusUpdate = false;

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "CERT_UPDATE";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");

            // Store trans
            username = usersEntity.getUsername();
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            String newFileName = frameworkUtils.generateUUID();

            List<CertificateEntity> certificateEntityList = certificateRepository.findByUsersAndCertCodeList(usersEntity.getMobileUuid(),  request.getDocumentCode());
            if(certificateEntityList.isEmpty()) {
                throw new BusinessException(AppStatus.DATA_IS_EXISTING, request.getDocumentCode());
            }

            // Update table
            CertificateEntity entity  = new CertificateEntity();
            entity.setCertMobileUuid(usersEntity.getMobileUuid());
            entity.setCertDocumentCode(request.getDocumentCode());
            entity.setCertStartDate(request.getCertStartDate());
            entity.setCertEndDate(request.getCertEndDate());
            entity.setCertStatus("A");
            entity.setUpdateDate(new Date());
            entity.setUpdateBy(usersEntity.getUsername());
            entity.setCertId(certificateEntityList.get(0).getCertId());

            // Is not change file cert.
            if(request.getIsChangeFile().equals("N")){
                isStatusUpdate  = certificateRepository.updateNoChangeFile(entity);
            } else {
                // is update file cert.
                entity.setCertFile(newFileName);
                entity.setOriginalFileName(request.getFileCertName());
                isStatusUpdate  = certificateRepository.update(entity);

                if(isStatusUpdate) {
                    // Upload file to S3.
                    String keyName =  String.format(storePathTemplate, usersEntity.getMobileUuid(), newFileName);
                    getS3.putObject(bucketName, keyName, request.getFileCert());
                    log.info("put object {} is success.", keyName);
                }
            }

            //Update cert is success
            if(isStatusUpdate) {

                // Set response
                response.setDocumentCode(request.getDocumentCode());
                response.setCertStartDate(request.getCertStartDate());
                response.setCertEndDateType(request.getCertEndDateType());
                response.setCertEndDate(request.getCertEndDate());
                response.setFileCertName(request.getFileCertName());

                log.info("Update Cert is success.");
            }

        } catch (CommonException ce){
            statusCode = ce.getCode();
            throw  ce;
        } catch(Exception ex){
            log.error("Document Update Exception {}", ex.getMessage());
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());
        } finally {
            String resJson = frameworkUtils.toObjectToJson(response);
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return response;
    }

    public String documentDelete(String certCode) {

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "CERT_DELETE";
        String username = "";

        try {
            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");

            // Store trans
            username = usersEntity.getUsername();
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            List<CertificateEntity> certificateEntityList = certificateRepository.findByUsersAndCertCodeList(usersEntity.getMobileUuid(),  certCode);
            if(certificateEntityList.isEmpty()) {
                throw new BusinessException(AppStatus.DATA_IS_EXISTING, certCode);
            }

            if(certificateRepository.documentDelete(usersEntity.getMobileUuid(), certCode)) {
                log.info("Delete Cert is success.");
            }

        } catch (CommonException ce){
            statusCode = ce.getCode();
            throw  ce;
        } catch(Exception ex){
            log.error("Document delete Exception {}", ex.getMessage());
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());
        } finally {
            String resJson = frameworkUtils.toObjectToJson("success");
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return "success";

    }

    public DocumentCreateResponse documentEdit(String certCode) {

        DocumentCreateResponse response = new DocumentCreateResponse();

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "CERT_EDIT";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");

            // Store trans
            username = usersEntity.getUsername();
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            List<CertificateEntity> certificateEntityList = certificateRepository.findByUsersAndCertCodeList(usersEntity.getMobileUuid(),  certCode);
            if(certificateEntityList.isEmpty()) {
                throw new BusinessException(AppStatus.DATA_IS_EXISTING, certCode);
            }

            CertificateEntity item  =  certificateEntityList.get(0);

            // Set response
            response.setDocumentCode(item.getCertDocumentCode());
            response.setCertStartDate(dateUtil.formatDateToStr(item.getCertStartDate(), DateUtil.YEAR_MONTH_DATE));

            if(null == item.getCertEndDate()) {
                response.setCertEndDateType("N");
                response.setCertEndDate("9999-99-99");
            } else {
                response.setCertEndDateType("A");
                response.setCertEndDate(dateUtil.formatDateToStr(item.getCertEndDate(),DateUtil.YEAR_MONTH_DATE));
            }

            response.setFileCertName(item.getOriginalFileName());

        } catch (CommonException ce){
            throw  ce;
        } catch(Exception ex){
            log.error("Document edit Exception {}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());
        }  finally {
            String resJson = frameworkUtils.toObjectToJson(response);
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return response;
    }

    public String viewCert(String certCode) {

        String response = "";

        try {
            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");

            CertificateEntity certificateEntity = certificateRepository.findBy(usersEntity.getMobileUuid(),  certCode);
            if(null == certificateEntity){
                throw new BusinessException(AppStatus.DATA_NOT_FOUND, usersEntity.getEmail() + " -> " + certCode);
            }

            String keyName =  String.format(storePathTemplate, usersEntity.getMobileUuid(), certificateEntity.getCertFile());
            response = getS3.getObjectAsString(bucketName, keyName);
            log.info("Load file cert -> {} is success.", keyName);
        } catch (CommonException ce){
            throw  ce;
        } catch(Exception ex){
            log.error("Document view Exception {}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());
        }
        return response;
    }

}
