package com.seaman.service;

import com.amazonaws.services.s3.AmazonS3;
import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.constant.DocumentRenewalAction;
import com.seaman.constant.DocumentRenewalStatus;
import com.seaman.entity.CertificateEntity;
import com.seaman.entity.DeliveryAddressEntity;
import com.seaman.entity.DocumentEntity;
import com.seaman.entity.DocumentRenewalRequestEntity;
import com.seaman.entity.DocumentRequestItemFileEntity;
import com.seaman.entity.DocumentRequestItemEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.exception.BusinessException;
import com.seaman.exception.CommonException;
import com.seaman.exception.MissingParameterException;
import com.seaman.model.request.DocumentCreateRequest;
import com.seaman.model.request.DocumentRequestValidateRequest;
import com.seaman.model.request.DocumentUpdateRequest;
import com.seaman.model.response.DocumentCreateResponse;
import com.seaman.model.response.DocumentRequestItemResponse;
import com.seaman.model.response.DocumentRequestValidateResponse;
import com.seaman.model.response.DocumentRenewalPriceResponse;
import com.seaman.model.response.DocumentUpdateResponse;
import com.seaman.model.response.DeliveryAddressResponse;
import com.seaman.model.response.PageDocumentResponse;
import com.seaman.repository.CertificateRepository;
import com.seaman.repository.DeliveryAddressRepository;
import com.seaman.repository.DocumentRenewalCreateRepository;
import com.seaman.repository.DocumentRenewalFoundationRepository;
import com.seaman.repository.DocumentRenewalRequestItemFileRepository;
import com.seaman.repository.DocumentRepository;
import com.seaman.repository.DocumentRequestItemFileRepository;
import com.seaman.utils.DateUtil;
import com.seaman.utils.Base64FileValidator;
import com.seaman.utils.FrameworkUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.servlet.http.HttpServletRequest;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Bangkok");

    private final HttpServletRequest httpServletRequest;
    private final DocumentRepository documentRepository;
    private final DocumentRenewalCreateRepository documentRenewalCreateRepository;
    private final DocumentRenewalFoundationRepository documentRenewalFoundationRepository;
    private final DocumentRenewalService documentRenewalService;
    private final DocumentRequestItemFileRepository documentRequestItemFileRepository;
    private final DocumentRequestItemFileService documentRequestItemFileService;
    private final DocumentRenewalRequestItemFileRepository documentRenewalRequestItemFileRepository;
    private final DocumentRenewalRequestItemFileService documentRenewalRequestItemFileService;
    private final CertificateRepository certificateRepository;
    private final DeliveryAddressRepository deliveryAddressRepository;
    private final DateUtil dateUtil;
    private final AmazonS3 getS3;
    private final FrameworkUtils frameworkUtils;
    private final Base64FileValidator base64FileValidator;

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

            base64FileValidator.validateDocument(request.getFileCert(), "fileCert");

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
                String keyName = String.format(storePathTemplate, usersEntity.getMobileUuid(), newFileName);
                getS3.putObject(bucketName, keyName, request.getFileCert());
                log.info("put object {} is success.", keyName);

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
                base64FileValidator.validateDocument(request.getFileCert(), "fileCert");

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

    @Transactional
    public DocumentRequestValidateResponse validateAndCreateDocumentRenewalsItems(DocumentRequestValidateRequest request) {

        DocumentRequestValidateResponse response = new DocumentRequestValidateResponse();
        long startedAt = System.nanoTime();
        long findDocumentMs = 0;
        long activeRequestMs = 0;
        long validateItemsMs = 0;
        long priceMs = 0;
        long addressMs = 0;
        long createRequestMs = 0;
        long insertItemsMs = 0;
        long mapItemsMs = 0;
        long responseLogJsonMs = 0;
        String timingPath = "create";

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "VALIDATE_AND_CREATE_DOCUMENT_RENEWALS_ITEMS";
        String username = "";
        boolean inserted = false;

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            if (usersEntity == null) {
                throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, "userObject not found in request");
            }
            username = usersEntity.getUsername();

            transactionLogsService.insert(transId, bodyReqJson, serviceName, username);
            inserted = true;

            String documentCode = request.getDocumentCode().trim().toUpperCase(Locale.ROOT);
            String idempotencyKey = normalizeIdempotencyKey(request.getIdempotencyKey());
            long segmentStartedAt = System.nanoTime();
            DocumentEntity document = documentRepository.findByDocumentCode(documentCode);
            String documentName = documentName(document, documentCode);
            findDocumentMs = elapsedMs(segmentStartedAt);

            if (idempotencyKey != null) {
                segmentStartedAt = System.nanoTime();
                DocumentRenewalRequestEntity idempotentRequest = documentRenewalCreateRepository
                        .findByIdempotencyKey(usersEntity.getMobileUuid(), idempotencyKey);
                activeRequestMs += elapsedMs(segmentStartedAt);
                if (idempotentRequest != null) {
                    if (!documentCode.equals(idempotentRequest.getDocumentCode())) {
                        throw new BusinessException(AppStatus.INVALID_FORMAT, "idempotencyKey");
                    }
                    timingPath = "idempotent";
                    segmentStartedAt = System.nanoTime();
                    response = buildValidateCreateResponse(idempotentRequest, documentCode, documentName,
                            usersEntity.getMobileUuid());
                    validateItemsMs = elapsedMs(segmentStartedAt);
                    return response;
                }
            }

            // ตรวจสอบการ renewal มีสถานะจัดส่งสำเร็จแล้วหรือไม่
            segmentStartedAt = System.nanoTime();
            DocumentRenewalRequestEntity activeRequest = documentRenewalCreateRepository
                    .findLatestActiveRequestNotDelivered(usersEntity.getMobileUuid(), documentCode);
            activeRequestMs = elapsedMs(segmentStartedAt);
            if (activeRequest != null) {
                timingPath = "existing";
                segmentStartedAt = System.nanoTime();
                response = buildValidateCreateResponse(activeRequest, documentCode, documentName,
                        usersEntity.getMobileUuid());
                validateItemsMs = elapsedMs(segmentStartedAt);
                return response;
            }

            segmentStartedAt = System.nanoTime();
            List<DocumentRequestItemEntity> items =
                    documentRepository.findMissingItemsByUserAndDocumentCode(
                            usersEntity.getMobileUuid(), documentCode);
            validateItemsMs = elapsedMs(segmentStartedAt);

            if (items == null || items.isEmpty()) {
                throw new MissingParameterException(AppStatus.DOCUMENT_SETTING_NOT_FOUND, "");
            }

            response.setDocumentCode(documentCode);
            response.setDocumentName(documentName);
            response.setMobileNumber(usersEntity.getMobileNumber());
            response.setEmail(usersEntity.getEmail());

            segmentStartedAt = System.nanoTime();
            DocumentRenewalPriceResponse price = documentRenewalService.price(documentCode);
            priceMs = elapsedMs(segmentStartedAt);
            segmentStartedAt = System.nanoTime();
            DeliveryAddressEntity address = defaultDeliveryAddressOrNull(usersEntity.getMobileUuid());
            addressMs = elapsedMs(segmentStartedAt);
            String requestId = frameworkUtils.generateUUID();
            String period = YearMonth.now(BUSINESS_ZONE).format(DateTimeFormatter.ofPattern("yyMM"));
            segmentStartedAt = System.nanoTime();
            String requestNo = documentRenewalCreateRepository.nextRequestNo(period);
            String statusId = documentRenewalFoundationRepository
                    .findActiveStatusId(DocumentRenewalStatus.PAYMENT_PENDING);

            try {
                documentRenewalCreateRepository.insertRequest(requestId, requestNo,
                        usersEntity.getMobileUuid(), usersEntity.getMobileNumber(), usersEntity.getEmail(),
                        documentCode, statusId, price.getPriceSettingId(),
                        address == null ? null : address.getId(), price.getTotal(), idempotencyKey);
            } catch (DuplicateKeyException ex) {
                if (idempotencyKey == null) {
                    throw ex;
                }
                DocumentRenewalRequestEntity idempotentRequest = documentRenewalCreateRepository
                        .findByIdempotencyKey(usersEntity.getMobileUuid(), idempotencyKey);
                if (idempotentRequest == null || !documentCode.equals(idempotentRequest.getDocumentCode())) {
                    throw ex;
                }
                timingPath = "idempotent-duplicate";
                response = buildValidateCreateResponse(idempotentRequest, documentCode, documentName,
                        usersEntity.getMobileUuid());
                return response;
            }
            createRequestMs = elapsedMs(segmentStartedAt);
            // ตรวจสอบที่อยู่ ณ ปัจจุบันเป็นข้อมูล address ที่อยู่ปัจจุบันของผู้ใช้งาน
            if (address != null) {
                documentRenewalCreateRepository.insertDeliveryAddressSnapshot(
                        requestId, address, usersEntity.getMobileNumber());
                response.setAddress(Collections.singletonList(mapDeliveryAddress(address, usersEntity.getMobileNumber())));
            }

            // เพิ่มเอกสารที่ต้องมีในการ renewals
            segmentStartedAt = System.nanoTime();
            int itemCount = documentRenewalCreateRepository.insertRequestItems(requestId, documentCode);
            if (itemCount != items.size()) {
                throw new BusinessException(AppStatus.EXCEPTION_DATABASE, "documentRenewalRequestItems");
            }
            documentRenewalFoundationRepository.appendTransaction(requestId, DocumentRenewalAction.CREATE,
                    null, DocumentRenewalStatus.PAYMENT_PENDING, "Unpaid draft created",
                    usersEntity.getMobileUuid());

            List<DocumentRequestItemEntity> createdItems = documentRenewalCreateRepository
                    .findRequestItemsForValidate(requestId, usersEntity.getMobileUuid());
            if (createdItems == null || createdItems.isEmpty()) {
                throw new MissingParameterException(AppStatus.DOCUMENT_SETTING_NOT_FOUND, "");
            }
            insertItemsMs = elapsedMs(segmentStartedAt);
            segmentStartedAt = System.nanoTime();
            response.setItems(mapDocumentRequestItems(createdItems));
            mapItemsMs = elapsedMs(segmentStartedAt);
            response.setRequestId(requestId);
            response.setRequestNo(requestNo);
            response.setIdempotencyKey(idempotencyKey);

        } catch (CommonException ce) {
            statusCode = ce.getCode();
            log.error("{} error -> {}", serviceName, ce.getMessage());
            throw ce;
        } catch (Exception ex) {
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            log.error("{} error -> {}", serviceName, ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());
        } finally {
            if (inserted) {
                long segmentStartedAt = System.nanoTime();
                String responseJson = frameworkUtils.toObjectToJson(response);
                responseLogJsonMs = elapsedMs(segmentStartedAt);
                transactionLogsService.update(transId, responseJson, statusCode, username);
            }
            String documentCode = request.getDocumentCode() == null
                    ? "" : request.getDocumentCode().trim().toUpperCase(Locale.ROOT);
            logValidateAndCreateTiming(startedAt, documentCode, activeRequestMs, findDocumentMs,
                    validateItemsMs, priceMs, addressMs, createRequestMs, insertItemsMs,
                    mapItemsMs, responseLogJsonMs, timingPath);
        }
        return response;
    }

    private String normalizeIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null) {
            return null;
        }
        String normalized = idempotencyKey.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private DocumentRequestValidateResponse buildValidateCreateResponse(
            DocumentRenewalRequestEntity request, String documentCode, String documentName,
            String mobileUserUuid) {
        List<DocumentRequestItemEntity> items = documentRenewalCreateRepository
                .findRequestItemsForValidate(request.getId(), mobileUserUuid);
        if (items == null || items.isEmpty()) {
            throw new MissingParameterException(AppStatus.DOCUMENT_SETTING_NOT_FOUND, "");
        }
        DocumentRequestValidateResponse response = new DocumentRequestValidateResponse();
        response.setRequestId(request.getId());
        response.setRequestNo(request.getRequestNo());
        response.setDocumentCode(documentCode);
        response.setDocumentName(documentName);
        response.setIdempotencyKey(request.getIdempotencyKey());
        response.setMobileNumber(request.getMobileNumber());
        response.setEmail(request.getEmail());
        response.setAddress(deliveryAddressSnapshot(request.getId(), mobileUserUuid));
        response.setItems(mapDocumentRequestItems(items));
        return response;
    }

    private long elapsedMs(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }

    private void logValidateAndCreateTiming(
            long startedAt, String documentCode, long activeRequestMs, long findDocumentMs,
            long validateItemsMs, long priceMs, long addressMs, long createRequestMs,
            long insertItemsMs, long mapItemsMs, long responseLogJsonMs, String path) {
        long totalMs = elapsedMs(startedAt);
        if (totalMs < 300) {
            return;
        }
        log.info("validateAndCreateDocumentRenewalsItems timing path={} documentCode={} totalMs={} "
                        + "findDocumentMs={} activeRequestMs={} validateItemsMs={} priceMs={} "
                        + "addressMs={} createRequestMs={} insertItemsMs={} mapItemsMs={} responseLogJsonMs={}",
                path, documentCode, totalMs, findDocumentMs, activeRequestMs, validateItemsMs,
                priceMs, addressMs, createRequestMs, insertItemsMs, mapItemsMs, responseLogJsonMs);
    }

    private DeliveryAddressEntity defaultDeliveryAddressOrNull(String mobileUserUuid) {
        List<DeliveryAddressEntity> addresses = deliveryAddressRepository.findActiveDefaults(mobileUserUuid);
        if (addresses == null || addresses.isEmpty()) {
            return null;
        }
        return addresses.get(0);
    }

    private List<DeliveryAddressResponse> deliveryAddressSnapshot(String requestId, String mobileUserUuid) {
        List<DeliveryAddressEntity> addresses = documentRenewalCreateRepository
                .findDeliveryAddressSnapshot(requestId, mobileUserUuid);
        if (addresses == null || addresses.isEmpty()) {
            return Collections.emptyList();
        }
        return addresses.stream()
                .map(address -> mapDeliveryAddress(address, address.getMobileNumber()))
                .collect(Collectors.toList());
    }

    private DeliveryAddressResponse mapDeliveryAddress(DeliveryAddressEntity entity, String mobileNumber) {
        DeliveryAddressResponse response = new DeliveryAddressResponse();
        response.setId(entity.getId());
        response.setFirstName(entity.getFirstName());
        response.setLastName(entity.getLastName());
        response.setAddressLine(entity.getAddressLine());
        response.setProvince(entity.getProvince());
        response.setDistrict(entity.getDistrict());
        response.setSubDistrict(entity.getSubDistrict());
        response.setPostalCode(entity.getPostalCode());
        response.setMobileNumber(mobileNumber);
        response.setDescription(entity.getDescription());
        response.setIsDefault(entity.getIsDefault());
        return response;
    }

    private List<DocumentRequestItemResponse> mapDocumentRequestItems(List<DocumentRequestItemEntity> items) {
        Map<String, List<DocumentRequestItemFileEntity>> requestFilesByItemId = requestFilesByItemId(items);
        Map<String, List<DocumentRequestItemFileEntity>> profileFilesByItemCode = profileFilesByItemCode(items);
        List<DocumentRequestItemResponse> responseList = new ArrayList<>();
        for (DocumentRequestItemEntity item : items) {
            DocumentRequestItemResponse dto = new DocumentRequestItemResponse();
            dto.setId(item.getId());
            dto.setProfileRequestItemId(item.getProfileRequestItemId());
            dto.setDocumentCode(item.getDocumentCode());
            dto.setDocumentMasterRequestItemCode(item.getDocumentMasterRequestItemCode());
            dto.setStorageScope(item.getStorageScope());
            dto.setDocumentType(item.getDocumentType());
            dto.setDocumentName(item.getDocumentName());
            dto.setDocumentStatus(item.getDocumentStatus());
            dto.setSortOrder(item.getSortOrder());
            dto.setFileUploaded(item.getFileUploaded());
            dto.setFilePath(item.getFilePath());
            dto.setCheckResult(item.getCheckResult());
            dto.setCheckNote(item.getCheckNote());
            if ("REQUEST".equals(item.getStorageScope())) {
                dto.setFiles(documentRenewalRequestItemFileService.mapFiles(
                        requestFilesByItemId.getOrDefault(item.getId(), Collections.emptyList())));
            } else if (item.getProfileRequestItemId() != null) {
                dto.setFiles(documentRequestItemFileService.mapFiles(
                        profileFilesByItemCode.getOrDefault(
                                item.getDocumentMasterRequestItemCode(), Collections.emptyList())));
            }

            if (item.getFileUploadedAt() != null) {
                dto.setFileUploadedAt(dateUtil.formatDateToString(item.getFileUploadedAt(), DateUtil.DATE_TIME));
            }

            responseList.add(dto);
        }
        return responseList;
    }

    private Map<String, List<DocumentRequestItemFileEntity>> requestFilesByItemId(
            List<DocumentRequestItemEntity> items) {
        List<String> requestItemIds = items.stream()
                .filter(item -> "REQUEST".equals(item.getStorageScope()))
                .filter(item -> Integer.valueOf(1).equals(item.getFileUploaded()))
                .map(DocumentRequestItemEntity::getId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (requestItemIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<DocumentRequestItemFileEntity> files =
                documentRenewalRequestItemFileRepository.findFilesByRequestItemIds(requestItemIds);
        if (files == null || files.isEmpty()) {
            return Collections.emptyMap();
        }
        return files.stream()
                .filter(file -> file.getRequestItemId() != null)
                .collect(Collectors.groupingBy(DocumentRequestItemFileEntity::getRequestItemId));
    }

    private Map<String, List<DocumentRequestItemFileEntity>> profileFilesByItemCode(
            List<DocumentRequestItemEntity> items) {
        Optional<String> mobileUserUuid = items.stream()
                .map(DocumentRequestItemEntity::getMobileUserUuid)
                .filter(Objects::nonNull)
                .findFirst();
        if (mobileUserUuid.isEmpty()) {
            return Collections.emptyMap();
        }
        List<String> itemCodes = items.stream()
                .filter(item -> !"REQUEST".equals(item.getStorageScope()))
                .filter(item -> item.getProfileRequestItemId() != null)
                .filter(item -> Integer.valueOf(1).equals(item.getFileUploaded()))
                .map(DocumentRequestItemEntity::getDocumentMasterRequestItemCode)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (itemCodes.isEmpty()) {
            return Collections.emptyMap();
        }
        List<DocumentRequestItemFileEntity> files =
                documentRequestItemFileRepository.findFilesByItemCodes(mobileUserUuid.get(), itemCodes);
        if (files == null || files.isEmpty()) {
            return Collections.emptyMap();
        }
        return files.stream()
                .filter(file -> file.getDocumentMasterRequestItemCode() != null)
                .collect(Collectors.groupingBy(DocumentRequestItemFileEntity::getDocumentMasterRequestItemCode));
    }

    private String documentName(DocumentEntity document, String fallback) {
        if (document == null) {
            return fallback;
        }
        String language = (String) httpServletRequest.getAttribute(AppSys.LANGUAGE);
        if ("EN".equalsIgnoreCase(language) && notBlank(document.getDocumentNameEn())) {
            return document.getDocumentNameEn();
        }
        if (notBlank(document.getDocumentNameTh())) {
            return document.getDocumentNameTh();
        }
        return notBlank(document.getDocumentNameEn()) ? document.getDocumentNameEn() : fallback;
    }

    private boolean notBlank(String value) {
        return value != null && !value.trim().isEmpty();
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
