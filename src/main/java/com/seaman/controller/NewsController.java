package com.seaman.controller;

import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.constant.Routes;
import com.seaman.model.common.SuccessResponse;
import com.seaman.model.response.NewsResponse;
import com.seaman.service.MessageCodeService;
import com.seaman.service.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "News", description = "ข่าวสารและประกาศ")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
public class NewsController extends  BaseController{

    private final MessageCodeService messageCodeService;

    private final NewsService newsService;

    @Operation(summary = "รายการข่าวสาร", description = "ดึงรายการข่าวสารและประกาศทั้งหมด")
    @GetMapping(Routes.NEWS)
    public ResponseEntity<SuccessResponse<NewsResponse>> listNews(HttpServletRequest httpServletRequest) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                newsService.listNews()
        ).build());
    }

    @Operation(summary = "รายละเอียดข่าว", description = "ดึงรายละเอียดของข่าวตาม ID")
    @GetMapping(Routes.NEWS_DETAIL)
    public ResponseEntity<SuccessResponse<NewsResponse>> newsDetail(HttpServletRequest httpServletRequest,
            @Parameter(description = "News ID", required = true) @RequestParam("newsId") String newsId) {

        String description = messageCodeService.getMessageDescription(AppStatus.SUCCESS_CODE, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));

        return ok(SuccessResponse.builder(
                AppStatus.SUCCESS_CODE,
                description,
                newsService.newsById(newsId)
        ).build());
    }

    @Operation(summary = "รูปภาพข่าว", description = "ดาวน์โหลดรูปภาพของข่าว")
    @GetMapping(value = Routes.PREVIEW_PIC_NEWS, produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getImage(
            @Parameter(description = "News ID", required = true) @RequestParam("newsId") String newsId) {

        String base64Image = newsService.previewNews(newsId);
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentLength(imageBytes.length);

        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
    }

}
