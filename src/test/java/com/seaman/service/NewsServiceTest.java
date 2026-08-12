package com.seaman.service;

import com.amazonaws.services.s3.AmazonS3;
import com.seaman.entity.NewsEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.model.response.NewsModel;
import com.seaman.model.response.NewsResponse;
import com.seaman.repository.NewsRepository;
import com.seaman.utils.DateUtil;
import com.seaman.utils.FrameworkUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsServiceTest {

    @Mock HttpServletRequest httpServletRequest;
    @Mock TransactionLogsService transactionLogsService;
    @Mock FrameworkUtils frameworkUtils;
    @Mock NewsRepository newsRepository;
    @Mock DateUtil dateUtil;
    @Mock AmazonS3 getS3;

    private NewsService service;
    private UsersEntity user;

    @BeforeEach
    void setUp() {
        service = new NewsService(httpServletRequest, transactionLogsService, frameworkUtils,
                newsRepository, dateUtil, getS3);
        ReflectionTestUtils.setField(service, "bucketName", "smart-seaman-bucket");
        ReflectionTestUtils.setField(service, "pathNewsImage", "news/images");

        user = new UsersEntity();
        user.setUsername("crew@example.com");
        lenient().when(httpServletRequest.getAttribute(anyString()))
                .thenAnswer(invocation -> "userObject".equals(invocation.getArgument(0)) ? user : null);
        lenient().when(frameworkUtils.toObjectToJson(any())).thenReturn("{}");
    }

    // ---- listNews

    @Test
    void listNewsSplitsShipAndGeneralAndBlanksBody() {
        NewsEntity shipEntity = newsEntity(1, "Ship News", "Ship body");
        NewsEntity generalEntity = newsEntity(2, "General News", "General body");
        when(newsRepository.findAll("SHIP")).thenReturn(List.of(shipEntity));
        when(newsRepository.findAll("GENERAL")).thenReturn(List.of(generalEntity));
        when(dateUtil.formatDateToString(any(Date.class), eq(DateUtil.DATE_TIME))).thenReturn("2026-08-08 10:00:00");

        NewsResponse response = service.listNews();

        assertEquals(1, response.getNewsShip().size());
        assertEquals("Ship News", response.getNewsShip().get(0).getTitle());
        assertEquals("", response.getNewsShip().get(0).getBody());
        assertEquals("2026-08-08 10:00:00", response.getNewsShip().get(0).getNewsDate());
        assertEquals(1, response.getNewsGeneral().size());
        assertEquals("General News", response.getNewsGeneral().get(0).getTitle());
        assertEquals("", response.getNewsGeneral().get(0).getBody());
    }

    @Test
    void listNewsHandlesEmptyListsIndependently() {
        when(newsRepository.findAll("SHIP")).thenReturn(Collections.emptyList());
        when(newsRepository.findAll("GENERAL")).thenReturn(Collections.emptyList());

        NewsResponse response = service.listNews();

        assertTrue(response.getNewsShip().isEmpty());
        assertTrue(response.getNewsGeneral().isEmpty());
    }

    // ---- newsById

    @Test
    void newsByIdReturnsFullDetailIncludingBody() {
        NewsEntity entity = newsEntity(1, "News Title", "Full details");
        when(newsRepository.findById("1")).thenReturn(entity);
        when(dateUtil.formatDateToString(any(Date.class), eq(DateUtil.DATE_TIME))).thenReturn("2026-08-08 10:00:00");

        NewsModel model = service.newsById("1");

        assertEquals("1", model.getId());
        assertEquals("News Title", model.getTitle());
        assertEquals("Full details", model.getBody());
        assertEquals("2026-08-08 10:00:00", model.getNewsDate());
    }

    @Test
    void newsByIdThrowsNpeWhenEntityNotFound() {
        // No null-guard here (unlike previewNews): a missing entity causes an NPE that propagates
        // through the generic catch(Exception) block.
        when(newsRepository.findById("missing")).thenReturn(null);

        assertThrows(NullPointerException.class, () -> service.newsById("missing"));
    }

    // ---- previewNews

    @Test
    void previewNewsReturnsEmptyStringWhenNotFound() {
        when(newsRepository.findById("1")).thenReturn(null);

        String result = service.previewNews("1");

        assertEquals("", result);
        verify(getS3, never()).getObjectAsString(anyString(), anyString());
    }

    @Test
    void previewNewsFetchesImageFromS3WhenFound() {
        NewsEntity entity = newsEntity(1, "News Title", "Body");
        entity.setNewsPictureFileName("picture.jpg");
        when(newsRepository.findById("1")).thenReturn(entity);
        when(getS3.getObjectAsString("smart-seaman-bucket", "news/images/picture.jpg"))
                .thenReturn("base64-image");

        String result = service.previewNews("1");

        assertEquals("base64-image", result);
    }

    // ---- fixtures

    private NewsEntity newsEntity(int id, String title, String details) {
        NewsEntity entity = new NewsEntity();
        entity.setNewsId(id);
        entity.setNewsTitle(title);
        entity.setNewsDetails(details);
        entity.setUpdateDate(new Date());
        return entity;
    }
}
