package com.seaman.service;

import com.amazonaws.services.s3.AmazonS3;
import com.seaman.entity.UsersEntity;
import com.seaman.entity.VoucherEntity;
import com.seaman.model.response.VoucherModel;
import com.seaman.model.response.VoucherResponse;
import com.seaman.repository.VoucherRepository;
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
class VoucherServiceTest {

    @Mock HttpServletRequest httpServletRequest;
    @Mock TransactionLogsService transactionLogsService;
    @Mock FrameworkUtils frameworkUtils;
    @Mock VoucherRepository voucherRepository;
    @Mock AmazonS3 getS3;

    private VoucherService service;
    private UsersEntity user;

    @BeforeEach
    void setUp() {
        service = new VoucherService(httpServletRequest, transactionLogsService, frameworkUtils,
                voucherRepository, getS3);
        ReflectionTestUtils.setField(service, "bucketName", "smart-seaman-bucket");
        ReflectionTestUtils.setField(service, "pathVoucherImage", "vouchers/images");
        ReflectionTestUtils.setField(service, "pathVoucherQR", "vouchers/qr");

        user = new UsersEntity();
        user.setUsername("crew@example.com");
        user.setSmartSeamanId("00001");
        lenient().when(httpServletRequest.getAttribute(anyString()))
                .thenAnswer(invocation -> "userObject".equals(invocation.getArgument(0)) ? user : null);
        lenient().when(frameworkUtils.toObjectToJson(any())).thenReturn("{}");
    }

    // ---- listVoucher

    @Test
    void listVoucherMapsEntitiesToModels() {
        VoucherEntity entity = voucherEntity();
        when(voucherRepository.findAll("00001")).thenReturn(List.of(entity));

        VoucherResponse response = service.listVoucher();

        assertEquals(1, response.getVoucherModel().size());
        assertEquals("V001", response.getVoucherModel().get(0).getVoucherId());
    }

    @Test
    void listVoucherReturnsEmptyListWhenNoneFound() {
        when(voucherRepository.findAll("00001")).thenReturn(Collections.emptyList());

        VoucherResponse response = service.listVoucher();

        assertTrue(response.getVoucherModel().isEmpty());
    }

    // ---- previewVoucher

    @Test
    void previewVoucherReturnsEmptyStringWhenNotFound() {
        when(voucherRepository.findById("V001")).thenReturn(null);

        String result = service.previewVoucher("V001");

        assertEquals("", result);
        verify(getS3, never()).getObjectAsString(anyString(), anyString());
    }

    @Test
    void previewVoucherFetchesImageFromS3WhenFound() {
        VoucherEntity entity = voucherEntity();
        when(voucherRepository.findById("V001")).thenReturn(entity);
        when(getS3.getObjectAsString("smart-seaman-bucket", "vouchers/images/picture.jpg"))
                .thenReturn("base64-image");

        String result = service.previewVoucher("V001");

        assertEquals("base64-image", result);
    }

    // ---- previewQrCode

    @Test
    void previewQrCodeReturnsImageWhenS3Succeeds() {
        VoucherEntity entity = voucherEntity();
        when(voucherRepository.findById("V001")).thenReturn(entity);
        when(getS3.getObjectAsString("smart-seaman-bucket", "vouchers/qr/qrcode.png"))
                .thenReturn("base64-qr");

        String result = service.previewQrCode("V001");

        assertEquals("base64-qr", result);
    }

    @Test
    void previewQrCodeSwallowsS3ExceptionAndReturnsNull() {
        VoucherEntity entity = voucherEntity();
        when(voucherRepository.findById("V001")).thenReturn(entity);
        when(getS3.getObjectAsString(anyString(), anyString())).thenThrow(new RuntimeException("s3 down"));

        String result = service.previewQrCode("V001");

        assertNull(result);
        verify(transactionLogsService).update(any(), any(), anyString(), anyString());
    }

    @Test
    void previewQrCodeThrowsWhenEntityNotFound() {
        when(voucherRepository.findById("V001")).thenReturn(null);

        // No null-guard on entity before accessing getVoucherQrcode(): the NPE is caught by the
        // inner try/catch (logged, swallowed) so the outer call still returns normally with null.
        String result = service.previewQrCode("V001");

        assertNull(result);
        verify(getS3, never()).getObjectAsString(anyString(), anyString());
    }

    // ---- voucherDetail

    @Test
    void voucherDetailMapsEntityFields() {
        VoucherEntity entity = voucherEntity();
        when(voucherRepository.findById("V001")).thenReturn(entity);

        VoucherModel model = service.voucherDetail("V001");

        assertEquals("V001", model.getVoucherId());
        assertEquals("10% Off", model.getVoucherTitle());
    }

    @Test
    void voucherDetailThrowsWhenEntityNotFound() {
        when(voucherRepository.findById("V001")).thenReturn(null);

        // No null-guard here: missing entity results in an NPE, wrapped by the outer
        // catch(Exception) which sets statusCode and rethrows.
        assertThrows(NullPointerException.class, () -> service.voucherDetail("V001"));
    }

    // ---- fixtures

    private VoucherEntity voucherEntity() {
        VoucherEntity entity = new VoucherEntity();
        entity.setVoucherId("V001");
        entity.setVoucherTitle("10% Off");
        entity.setVoucherDetails("10% off next purchase");
        entity.setVoucherStartDate("2026-01-01");
        entity.setVoucherEndDate("2026-12-31");
        entity.setVoucherPicture("picture.jpg");
        entity.setVoucherQrcode("qrcode.png");
        return entity;
    }
}
