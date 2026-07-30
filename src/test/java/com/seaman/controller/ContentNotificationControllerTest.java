package com.seaman.controller;

import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.model.common.SuccessResponse;
import com.seaman.model.request.FcmNotificationRequest;
import com.seaman.model.request.MSendNotificationsRequest;
import com.seaman.model.request.SendNotificationRequest;
import com.seaman.model.response.*;
import com.seaman.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ContentNotificationControllerTest {
    private static final String PNG_BASE64 =
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADElEQVR42mP8z8AARQAFAAH+Af9qAAAAAElFTkSuQmCC";

    private HttpServletRequest request;
    private MessageCodeService messages;

    @BeforeEach
    void setUp() {
        request = mock(HttpServletRequest.class);
        messages = mock(MessageCodeService.class);
        when(request.getAttribute(AppSys.LANGUAGE)).thenReturn("TH");
        when(messages.getMessageDescription(AppStatus.SUCCESS_CODE, "TH")).thenReturn("success");
    }

    @Test
    void newsEndpointsDelegateToService() {
        NewsService service = mock(NewsService.class);
        NewsController controller = new NewsController(messages, service);
        NewsResponse data = new NewsResponse();
        NewsModel detail = new NewsModel();
        when(service.listNews()).thenReturn(data);
        when(service.newsById("news-id")).thenReturn(detail);
        when(service.previewNews("news-id")).thenReturn(PNG_BASE64);

        assertSame(data, controller.listNews(request).getBody().getData());
        assertSame(detail, controller.newsDetail(request, "news-id").getBody().getData());
        ResponseEntity<byte[]> image = controller.getImage("news-id");
        assertEquals(MediaType.IMAGE_PNG, image.getHeaders().getContentType());
        assertTrue(image.getBody().length > 0);
    }

    @Test
    void bannerEndpointsDelegateToService() throws Exception {
        BannerService service = mock(BannerService.class);
        BannerController controller = new BannerController(messages, service);
        BannerResponse data = new BannerResponse();
        when(service.listBanner()).thenReturn(data);
        when(service.previewBanner("banner-id")).thenReturn(PNG_BASE64);

        assertSame(data, controller.listBanner(request).getBody().getData());
        HttpEntity<byte[]> image = controller.getImageBanner("banner-id");
        assertEquals(MediaType.IMAGE_PNG, image.getHeaders().getContentType());
        assertTrue(image.getBody().length > 0);
    }

    @Test
    void voucherEndpointsDelegateToService() throws Exception {
        VoucherService service = mock(VoucherService.class);
        VoucherController controller = new VoucherController(messages, service);
        VoucherResponse list = new VoucherResponse();
        VoucherModel detail = new VoucherModel();
        when(service.listVoucher()).thenReturn(list);
        when(service.voucherDetail("voucher-id")).thenReturn(detail);
        when(service.previewVoucher("voucher-id")).thenReturn(PNG_BASE64);
        when(service.previewQrCode("voucher-id")).thenReturn(PNG_BASE64);

        assertSame(list, controller.listNews(request).getBody().getData());
        assertSame(detail, controller.voucherDetail(request, "voucher-id").getBody().getData());
        assertEquals(MediaType.IMAGE_PNG, controller.getImage("voucher-id").getHeaders().getContentType());
        assertEquals(MediaType.IMAGE_PNG, controller.getQR("voucher-id").getHeaders().getContentType());
    }

    @Test
    void formAndSchoolEndpointsDelegateToService() throws Exception {
        FormService forms = mock(FormService.class);
        FormController formController = new FormController(messages, forms);
        FormResponse formData = new FormResponse();
        when(forms.formAll(request)).thenReturn(formData);
        when(forms.downloadForm(request, "form-id")).thenReturn("%PDF-1.4\n%%EOF".getBytes());
        assertSame(formData, formController.listSchoolTraining(request).getBody().getData());
        assertEquals(MediaType.APPLICATION_PDF,
                formController.getImage(request, "form-id").getHeaders().getContentType());

        SchoolTrainingService schools = mock(SchoolTrainingService.class);
        SchoolController schoolController = new SchoolController(messages, schools);
        SchoolTrainingResponse schoolData = new SchoolTrainingResponse();
        when(schools.listSchoolTraining(request, "COURSE001")).thenReturn(schoolData);
        when(schools.schoolTrainingDetail(request, "COMP001", "COURSE001")).thenReturn(schoolData);
        assertSame(schoolData,
                schoolController.listSchoolTraining(request, "COURSE001").getBody().getData());
        assertSame(schoolData,
                schoolController.schoolTrainingDetail(request, "COMP001", "COURSE001").getBody().getData());
    }

    @Test
    void notificationAndFcmEndpointsDelegateToService() {
        SendNotificationService notifications = mock(SendNotificationService.class);
        NotiController notiController = new NotiController(messages, notifications);
        NotificationResponse list = new NotificationResponse();
        UpdateNotificationsResponse update = new UpdateNotificationsResponse();
        MSendNotificationsRequest bulkRequest = new MSendNotificationsRequest();
        SendNotificationRequest singleRequest = new SendNotificationRequest();
        when(notifications.notifications(request)).thenReturn(list);
        when(notifications.updateNotifications(request, bulkRequest)).thenReturn(update);
        when(notifications.updateNotificationsByValueId(request, singleRequest)).thenReturn(update);
        when(notifications.updateAllNotifications(request)).thenReturn(update);

        assertEquals("success", notiController.sendNotiManual(request).getBody().getData());
        assertSame(list, notiController.notifications(request).getBody().getData());
        assertSame(update, notiController.updateNotifications(request, bulkRequest).getBody().getData());
        assertSame(update, notiController.updateNotificationsByValueId(request, singleRequest).getBody().getData());
        assertSame(update, notiController.updateNotifications(request).getBody().getData());
        verify(notifications).sendNotification();

        FcmNotificationService fcm = mock(FcmNotificationService.class);
        FcmController fcmController = new FcmController(messages, fcm);
        FcmNotificationRequest fcmRequest = new FcmNotificationRequest();
        fcmRequest.setTokenFcm("token-1");
        when(fcm.fcmUpdate(request, "token-1")).thenReturn("updated");
        assertSame("updated",
                ((SuccessResponse<?>) fcmController.listSchoolTraining(request, fcmRequest).getBody()).getData());
    }

    @Test
    void emailAndWebhookControllersDelegateToService() {
        EmailService email = mock(EmailService.class);
        EmailController emailController = new EmailController(messages, email);
        when(email.sendEmailRegister(anyString(), anyString(), anyString(), eq(""))).thenReturn("sent");
        assertSame("sent", emailController.sendEmail(request).getBody().getData());

        OmiseWebhookService webhook = mock(OmiseWebhookService.class);
        OmiseWebhookController webhookController = new OmiseWebhookController(webhook);
        assertEquals(200, webhookController.omise("{}", "sig", "ts").getStatusCodeValue());
        verify(webhook).handle("{}", "sig", "ts");
    }
}
