package com.seaman.service;

import com.amazonaws.services.s3.AmazonS3;
import com.google.zxing.WriterException;
import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.entity.UsersEntity;
import com.seaman.entity.VoucherEntity;
import com.seaman.exception.CommonException;
import com.seaman.model.response.VoucherModel;
import com.seaman.model.response.VoucherResponse;
import com.seaman.repository.VoucherRepository;
import com.seaman.utils.FrameworkUtils;
import com.seaman.utils.QRCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VoucherService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final HttpServletRequest httpServletRequest;
    private final TransactionLogsService transactionLogsService;
    private final FrameworkUtils frameworkUtils;
    private final VoucherRepository voucherRepository;

    private final AmazonS3 getS3;

    @Value("${object.store.bucket}")
    private String bucketName;

    @Value("${object.store.path.voucher}")
    private String pathVoucherImage;

    @Value("${object.store.path.voucher.qr}")
    private String pathVoucherQR;

    public VoucherResponse listVoucher() {

        VoucherResponse response = new VoucherResponse();

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "VOUCHER CHECKED";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            log.info("Module -> {} Start. By username -> {}", serviceName,  username);

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            List<VoucherEntity> entities = voucherRepository.findAll(usersEntity.getSmartSeamanId());

            List<VoucherModel> models =  new ArrayList<>();

            for(VoucherEntity entity : entities){
                VoucherModel model =  new VoucherModel();
                model.setVoucherId(entity.getVoucherId());
                model.setVoucherTitle(entity.getVoucherTitle());
                model.setVoucherDetails(entity.getVoucherDetails());
                model.setVoucherStartDate(entity.getVoucherStartDate());
                model.setVoucherEndDate(entity.getVoucherEndDate());
                models.add(model);
            }

            response.setVoucherModel(models);

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
            log.info("Module -> {} End. By username -> {}", serviceName,  username);
        }
        return response;
    }

    public String previewVoucher(String id) {

        String imageBase64 = "";

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "VOUCHER IMAGE";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            log.info("Module -> {} Start. By username -> {}", serviceName,  username);

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            VoucherEntity entity = voucherRepository.findById(id);

            if(entity != null ) {
                String keyName = pathVoucherImage + "/" + entity.getVoucherPicture();
                imageBase64 = getS3.getObjectAsString(bucketName, keyName);
            }

        } catch (CommonException ce) {
            log.error("{}", ce.getMessage());
            statusCode = ce.getCode();
            throw ce;
        } catch (Exception ex) {
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            log.error("{} error -> {}", serviceName, ex);
            throw ex;
        } finally {
            String resJson = "{\"voucher_id\" :  \"" + id + "\"}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
            log.info("Module -> {} end. By username -> {}", serviceName,  username);
        }
        return imageBase64;
    }

    public String previewQrCode(String id) {

        String imageBase64 = null;

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "VOUCHER DETAILS CHECKED";
        String username = "";

        try {
            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            log.info("Module -> {} Start. By username -> {}", serviceName,  username);

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());
            log.info("Start caller service -> 1 DateTime : {}", new Date());

            VoucherEntity entity = voucherRepository.findById(id);
            boolean isFindS3 = false;

            try {
                String keyName = pathVoucherQR + "/" + entity.getVoucherQrcode();
                imageBase64 = getS3.getObjectAsString(bucketName, keyName);
                log.info("Start caller service -> 2 DateTime : {}", new Date());
                isFindS3 =  true;
            } catch(Exception ex){
                log.error("Voucher get QR Code. {}", String.valueOf(ex));
            }

            if(isFindS3) log.info("Get Image voucher QR code is success.");
            log.info("Start caller service -> 3 DateTime : {}", new Date());
            /**
             * use case for not image.
             */
//            if(!isFindS3) {
//                byte[] image = new byte[0];
//                // Template QR Voucher
//                StringBuilder templateVoucher =  new StringBuilder();
//                templateVoucher.append(entity.getVoucherQrcode());
//                image = QRCodeGenerator.getQRCodeImage(templateVoucher.toString(), 250, 250);
//                imageBase64 = Base64.getEncoder().encodeToString(image);
//            }

        } catch (CommonException ce) {
            log.error("{}", ce.getMessage());
            statusCode = ce.getCode();
            throw ce;
        } catch (Exception ex) {
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            log.error("{} error -> {}", serviceName, ex);
            throw ex;
        } finally {
            String resJson = "{\"qr_id\" : \"" + id + "\"}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
            log.info("Module -> {} end. By username -> {}", serviceName,  username);
        }
        return imageBase64;
    }

    public VoucherModel voucherDetail(String voucherId) {

        VoucherModel model =  new VoucherModel();

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "VOUCHER DETAIL";
        String username = "";

        byte[] image = new byte[0];

        try {
            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            log.info("Module -> {} Start. By username -> {}", serviceName,  username);

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            VoucherEntity entity = voucherRepository.findById(voucherId);

            model.setVoucherId(entity.getVoucherId());
            model.setVoucherTitle(entity.getVoucherTitle());
            model.setVoucherDetails(entity.getVoucherDetails());
            model.setVoucherStartDate(entity.getVoucherStartDate());
            model.setVoucherEndDate(entity.getVoucherEndDate());

        } catch (CommonException ce) {
            log.error("{}", ce.getMessage());
            statusCode = ce.getCode();
            throw ce;
        } catch (Exception ex) {
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            log.error("{} error -> {}", serviceName, ex);
            throw ex;
        } finally {
            String resJson = "{\"voucher_id\": \"" + voucherId + "\"}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
            log.info("Module -> {} End. By username -> {}", serviceName,  username);
        }

        return model;
    }
}