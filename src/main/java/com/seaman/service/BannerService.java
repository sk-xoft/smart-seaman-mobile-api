package com.seaman.service;

import com.amazonaws.services.s3.AmazonS3;
import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.entity.BannerEntity;
import com.seaman.entity.NewsEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.exception.CommonException;
import com.seaman.model.response.BannerModel;
import com.seaman.model.response.BannerResponse;
import com.seaman.repository.BannerRepository;
import com.seaman.utils.FrameworkUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BannerService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final HttpServletRequest httpServletRequest;
    private final TransactionLogsService transactionLogsService;
    private final FrameworkUtils frameworkUtils;
    private final AmazonS3 getS3;
    private final BannerRepository bannerRepository;

    @Value("${object.store.bucket}")
    private String bucketName;

    @Value("${object.store.path.banner}")
    private String pathBannerImage;

    public BannerResponse listBanner() {

        BannerResponse response = new BannerResponse();

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "LIST BANNER";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            List<BannerEntity> entitys = bannerRepository.findAll();
            List<BannerModel> result =  new ArrayList<>();

            for(BannerEntity entity : entitys){
                BannerModel model =  new BannerModel();
                model.setBannerId(entity.getBannerId());
                model.setBannerFileName(entity.getBannerFileName());
                result.add(model);
            }

            response.setBanners(result);
        } catch (CommonException ce) {
            log.error("{}", ce.getMessage());
            statusCode = ce.getCode();
            throw ce;
        } catch (Exception ex) {
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            log.error("List Banner -> {}", ex);
            throw ex;
        } finally {
            String resJson = frameworkUtils.toObjectToJson(response);
            transactionLogsService.update(transId, resJson, statusCode, username);
        }
        return response;
    }

    public String previewBanner(String id) {

        String imageBase64 = "";

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "BANNER IMAGE";
        String username = "";

        try {
            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            BannerEntity entity = bannerRepository.findById(id);

            String keyName = pathBannerImage + "/" + entity.getBannerFileName();
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
            String resJson = "{\"banner_id\": \"" + id + "\"}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return imageBase64;
    }
}
