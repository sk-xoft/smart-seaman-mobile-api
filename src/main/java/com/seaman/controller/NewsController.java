package com.seaman.controller;

import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.constant.Routes;
import com.seaman.model.common.SuccessResponse;
import com.seaman.model.response.NewsResponse;
import com.seaman.service.MessageCodeService;
import com.seaman.service.NewsService;
import lombok.RequiredArgsConstructor;
import net.sf.jmimemagic.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import java.util.Base64;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequiredArgsConstructor
public class NewsController extends  BaseController{

    private final MessageCodeService messageCodeService;

    private final NewsService newsService;

    @GetMapping(Routes.NEWS)
    public ResponseEntity<SuccessResponse<NewsResponse>> listNews(HttpServletRequest httpServletRequest) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                newsService.listNews()
        ).build());
    }

    @GetMapping(Routes.NEWS_DETAIL)
    public ResponseEntity<SuccessResponse<NewsResponse>> newsDetail(HttpServletRequest httpServletRequest, @RequestParam("newsId") String newsId) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                newsService.newsById(newsId)
        ).build());
    }

//    @GetMapping(Routes.PREVIEW_PIC_NEWS)
//    @ResponseStatus(HttpStatus.OK)
//    public HttpEntity<byte[]> getImageNews(@RequestParam("newsId") String newsId) throws MagicMatchNotFoundException, MagicException, MagicParseException {
//
//        String fileBase64 = newsService.previewNews(newsId);
//
//        // 1. download img your location...
//        byte[] content = Base64.getDecoder().decode(fileBase64);
//
//        MagicMatch match = Magic.getMagicMatch(content);
//        String mimeType = match.getMimeType();
//        HttpHeaders headers = new HttpHeaders();
//
//        if("image/png".equals(mimeType)) {
//            headers.setContentType(MediaType.IMAGE_PNG);
//        }
//
//        if("image/jpeg".equals(mimeType)) {
//            headers.setContentType(MediaType.IMAGE_JPEG);
//        }
//
//        if("application/pdf".equals(mimeType)) {
//            headers.setContentType(MediaType.APPLICATION_PDF);
//        }
//
//        headers.setContentLength(content.length);
//
//        return new HttpEntity<byte[]>(content, headers);
//    }

    @GetMapping(value = Routes.PREVIEW_PIC_NEWS, produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getImage(@RequestParam("newsId") String newsId) {

        // Replace with your Base64 encoded image string
        String base64Image = newsService.previewNews(newsId);
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentLength(imageBytes.length);

        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
    }

}
