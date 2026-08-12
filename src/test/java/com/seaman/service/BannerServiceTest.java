package com.seaman.service;

import com.amazonaws.services.s3.AmazonS3;
import com.seaman.entity.BannerEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.model.response.BannerResponse;
import com.seaman.repository.BannerRepository;
import com.seaman.utils.FrameworkUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BannerServiceTest {

    @Mock HttpServletRequest httpServletRequest;
    @Mock TransactionLogsService transactionLogsService;
    @Mock FrameworkUtils frameworkUtils;
    @Mock AmazonS3 getS3;
    @Mock BannerRepository bannerRepository;

    private BannerService service;
    private UsersEntity user;

    @BeforeEach
    void setUp() {
        service = new BannerService(httpServletRequest, transactionLogsService, frameworkUtils,
                getS3, bannerRepository);
        ReflectionTestUtils.setField(service, "bucketName", "smart-seaman-bucket");
        ReflectionTestUtils.setField(service, "pathBannerImage", "banners/images");

        user = new UsersEntity();
        user.setUsername("crew@example.com");
        lenient().when(httpServletRequest.getAttribute(anyString()))
                .thenAnswer(invocation -> "userObject".equals(invocation.getArgument(0)) ? user : null);
        lenient().when(frameworkUtils.toObjectToJson(any())).thenReturn("{}");
    }

    // ---- listBanner

    @Test
    void listBannerMapsEntitiesToModels() {
        BannerEntity entity = new BannerEntity();
        entity.setBannerId("B001");
        entity.setBannerFileName("banner.jpg");
        when(bannerRepository.findAll()).thenReturn(List.of(entity));

        BannerResponse response = service.listBanner();

        assertEquals(1, response.getBanners().size());
        assertEquals("B001", response.getBanners().get(0).getBannerId());
        assertEquals("banner.jpg", response.getBanners().get(0).getBannerFileName());
    }

    @Test
    void listBannerReturnsEmptyListWhenNoneFound() {
        when(bannerRepository.findAll()).thenReturn(Collections.emptyList());

        BannerResponse response = service.listBanner();

        assertTrue(response.getBanners().isEmpty());
    }

    // ---- previewBanner

    @Test
    void previewBannerFetchesImageFromS3() {
        BannerEntity entity = new BannerEntity();
        entity.setBannerId("B001");
        entity.setBannerFileName("banner.jpg");
        when(bannerRepository.findById("B001")).thenReturn(entity);
        when(getS3.getObjectAsString("smart-seaman-bucket", "banners/images/banner.jpg"))
                .thenReturn("base64-image");

        String result = service.previewBanner("B001");

        assertEquals("base64-image", result);
    }

    @Test
    void previewBannerThrowsWhenEntityNotFound() {
        // No null-guard here: missing entity results in an NPE, wrapped by the outer
        // catch(Exception) which sets statusCode and rethrows.
        when(bannerRepository.findById("missing")).thenReturn(null);

        assertThrows(NullPointerException.class, () -> service.previewBanner("missing"));
    }

    @Test
    void previewBannerPropagatesS3Failure() {
        BannerEntity entity = new BannerEntity();
        entity.setBannerId("B001");
        entity.setBannerFileName("banner.jpg");
        when(bannerRepository.findById("B001")).thenReturn(entity);
        when(getS3.getObjectAsString(anyString(), anyString())).thenThrow(new RuntimeException("s3 down"));

        assertThrows(RuntimeException.class, () -> service.previewBanner("B001"));
    }
}
