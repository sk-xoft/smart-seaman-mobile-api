package com.seaman.service;

import com.amazonaws.services.s3.AmazonS3;
import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.entity.NewsEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.exception.CommonException;
import com.seaman.model.response.NewsModel;
import com.seaman.model.response.NewsResponse;
import com.seaman.repository.NewsRepository;
import com.seaman.utils.DateUtil;
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
public class NewsService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final HttpServletRequest httpServletRequest;
    private final TransactionLogsService transactionLogsService;
    private final FrameworkUtils frameworkUtils;
    private final NewsRepository newsRepository;
    private final DateUtil dateUtil;
    private final AmazonS3 getS3;

    @Value("${object.store.bucket}")
    private String bucketName;

    @Value("${object.store.path.news}")
    private String pathNewsImage;

    public NewsResponse listNews() {

        NewsResponse response = new NewsResponse();

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "NEWS CHECKED";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            List<NewsEntity> entityShip = newsRepository.findAll( "SHIP");
            List<NewsEntity> entityGeneral = newsRepository.findAll( "GENERAL");

            List<NewsModel> modelShip =  new ArrayList<>();
            List<NewsModel> modelGeneral =  new ArrayList<>();

            for(NewsEntity entity : entityShip){
                 NewsModel model =  new NewsModel();
                 model.setId(String.valueOf(entity.getNewsId()));
                 model.setTitle(entity.getNewsTitle());
                 model.setBody("");
                 model.setNewsDate(dateUtil.formatDateToString(entity.getUpdateDate(), DateUtil.DATE_TIME));
                 modelShip.add(model);
            }

            for(NewsEntity entity : entityGeneral){
                NewsModel model =  new NewsModel();
                model.setId(String.valueOf(entity.getNewsId()));
                model.setTitle(entity.getNewsTitle());
                model.setBody("");
                model.setNewsDate(dateUtil.formatDateToString(entity.getUpdateDate(), DateUtil.DATE_TIME));
                modelGeneral.add(model);
            }

            response.setNewsShip(modelShip);
            response.setNewsGeneral(modelGeneral);

        } catch (CommonException ce) {
            log.error("{}", ce.getMessage());
            statusCode = ce.getCode();
            throw ce;
        } catch (Exception ex) {
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw ex;
        } finally {
            String resJson = frameworkUtils.toObjectToJson(response);
            transactionLogsService.update(transId, resJson, statusCode, username);
        }
        return response;
    }


    public NewsModel newsById (String id) {

        NewsModel response = new NewsModel();

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "NEWS READING";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            NewsEntity entity = newsRepository.findById(id);

            response.setId(id);
            response.setTitle(entity.getNewsTitle());
            response.setBody(entity.getNewsDetails());
            response.setNewsDate(dateUtil.formatDateToString(entity.getUpdateDate(), DateUtil.DATE_TIME));

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

    public String previewNews(String id) {

        String imageBase64 = "";

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "NEWS IMAGE";
        String username = "";

        try {
            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            NewsEntity entity = newsRepository.findById(id);

            if(entity != null){
                String keyName = pathNewsImage + "/" + entity.getNewsPictureFileName();
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
            String resJson = "{\"news_id\" :  \"" + id + "\"}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return imageBase64;
    }

}
