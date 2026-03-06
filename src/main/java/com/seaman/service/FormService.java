package com.seaman.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.entity.FormEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.exception.BusinessException;
import com.seaman.exception.CommonException;
import com.seaman.model.response.FormResponse;
import com.seaman.repository.FormRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FormService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final FormRepository formRepository;
    private final TransactionLogsService transactionLogsService;

    @Value("${object.store.bucket}")
    private String bucketName;

    @Value("${object.store.path.documents.download}")
    private String downloadFile;

    private final AmazonS3 getS3;

    public FormResponse formAll(HttpServletRequest httpServletRequest) {

        FormResponse response = new FormResponse();

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "FORM CHECKED";
        String username = "";

        try {
            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            List<FormEntity> listForm = formRepository.findAll();
            response.setForms(listForm);

        } catch (CommonException ce) {
            log.error("{}", ce.getMessage());
            throw ce;
        } catch (Exception ex) {
            log.error("{} error -> {}", serviceName, ex);
            throw ex;
        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return response;
    }

    public byte[] downloadForm(HttpServletRequest httpServletRequest, String id) {

        byte[] fileDownload;

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "FORM DOWNLOADED";
        String username = "";

        try {
            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            FormEntity entity = formRepository.findById(id);

            String keyName =  downloadFile + "/" + entity.getFormFileName();

            // วิธี Load file
            // S3Object s3Object = getS3.getObject(bucketName, keyName);
            // fileDownload = IOUtils.toByteArray(s3Object.getObjectContent());

            String fileBase64 = getS3.getObjectAsString(bucketName, keyName);
            fileDownload = Base64.getDecoder().decode(fileBase64);

        } catch (CommonException ce) {
            log.error("{}", ce.getMessage());
            statusCode = ce.getCode();
            throw ce;
        } catch (Exception ex) {
            log.error("{} error -> {}", serviceName, ex);
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw ex;
        } finally {
            String resJson = "{ \"formId\" :  \"" + id + "\"}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }
        return fileDownload;
    }


}
