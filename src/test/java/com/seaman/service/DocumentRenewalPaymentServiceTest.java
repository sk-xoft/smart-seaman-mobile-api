package com.seaman.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seaman.constant.DocumentRenewalStatus;
import com.seaman.entity.DocumentRenewalRequestEntity;
import com.seaman.entity.PaymentTransactionEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.exception.BusinessException;
import com.seaman.model.external.response.OmiseChargeResponse;
import com.seaman.model.request.DocumentRenewalPaymentRequest;
import com.seaman.model.response.DocumentRenewalPaymentResponse;
import com.seaman.repository.DocumentRenewalFoundationRepository;
import com.seaman.repository.DocumentRenewalPaymentRepository;
import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class DocumentRenewalPaymentServiceTest {

    @Test
    void createPromptPayUsesServerAmountAndPersistsOmiseCharge() throws Exception {
        DocumentRenewalFoundationRepository foundation = mock(DocumentRenewalFoundationRepository.class);
        DocumentRenewalPaymentRepository payments = mock(DocumentRenewalPaymentRepository.class);
        OmisePaymentClient omise = mock(OmisePaymentClient.class);
        HttpServletRequest http = mock(HttpServletRequest.class);
        DocumentRenewalPaymentService service =
                new DocumentRenewalPaymentService(foundation, payments, omise, http);
        UsersEntity user = new UsersEntity();
        user.setMobileUuid("mobile-user-uuid");
        when(http.getAttribute("userObject")).thenReturn(user);
        DocumentRenewalRequestEntity request = new DocumentRenewalRequestEntity();
        request.setId("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        request.setRequestNo("260700001");
        request.setAmount(new BigDecimal("1500.00"));
        request.setStatusNameEn(DocumentRenewalStatus.PAYMENT_PENDING.getMasterNameEn());
        when(foundation.lockOwnedRequest("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", "mobile-user-uuid"))
                .thenReturn(request);
        OmiseChargeResponse charge = new OmiseChargeResponse();
        charge.setId("chrg_test_1");
        charge.setSourceId("src_test_1");
        charge.setStatus("pending");
        charge.setQrCodeDownloadUri("https://api.omise.co/qr.svg");
        charge.setExpiresAt(new Date());
        charge.setRaw(new ObjectMapper().readTree("{\"id\":\"chrg_test_1\",\"status\":\"pending\"}"));
        when(omise.createCharge(eq(new BigDecimal("1500.00")), eq("260700001"), any()))
                .thenReturn(charge);
        when(omise.rawJson(charge.getRaw())).thenReturn("{\"id\":\"chrg_test_1\"}");
        DocumentRenewalPaymentRequest input = new DocumentRenewalPaymentRequest();
        input.setPaymentMethod("promptpay");
        input.setIdempotencyKey("renewal-260700001-1");

        DocumentRenewalPaymentResponse response =
                service.create("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", input);

        assertEquals("260700001", response.getRequestNo());
        assertEquals("PENDING", response.getStatus());
        assertEquals("PROMPTPAY", response.getChannel());
        assertEquals("https://api.omise.co/qr.svg", response.getQrCodeDownloadUri());
        verify(payments).insert(argThat(entity ->
                new BigDecimal("1500.00").compareTo(entity.getAmount()) == 0
                        && "260700001".equals(entity.getRequestNo())
                        && "chrg_test_1".equals(entity.getProviderChargeId())
                        && "src_test_1".equals(entity.getProviderSourceId())));
    }

    @Test
    void retryWithSameIdempotencyKeyReturnsExistingPaymentWithoutNewCharge() {
        DocumentRenewalFoundationRepository foundation = mock(DocumentRenewalFoundationRepository.class);
        DocumentRenewalPaymentRepository payments = mock(DocumentRenewalPaymentRepository.class);
        OmisePaymentClient omise = mock(OmisePaymentClient.class);
        HttpServletRequest http = mock(HttpServletRequest.class);
        DocumentRenewalPaymentService service =
                new DocumentRenewalPaymentService(foundation, payments, omise, http);
        UsersEntity user = new UsersEntity();
        user.setMobileUuid("mobile-user-uuid");
        when(http.getAttribute("userObject")).thenReturn(user);
        DocumentRenewalRequestEntity request = new DocumentRenewalRequestEntity();
        request.setId("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        request.setStatusNameEn(DocumentRenewalStatus.PAYMENT_PENDING.getMasterNameEn());
        when(foundation.lockOwnedRequest("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", "mobile-user-uuid"))
                .thenReturn(request);
        PaymentTransactionEntity existing = new PaymentTransactionEntity();
        existing.setId("11111111-1111-1111-1111-111111111111");
        existing.setRequestId("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        existing.setRequestNo("260700001");
        existing.setStatus("PENDING");
        existing.setChannel("PROMPTPAY");
        existing.setPaymentMethod("promptpay");
        existing.setAmount(new BigDecimal("1500.00"));
        existing.setCurrency("THB");
        existing.setProvider("OMISE");
        when(payments.findByIdempotencyKey(
                "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", "retry-key", "mobile-user-uuid"))
                .thenReturn(existing);
        when(omise.qrCodeDownloadUri(null)).thenReturn(null);
        DocumentRenewalPaymentRequest input = new DocumentRenewalPaymentRequest();
        input.setPaymentMethod("promptpay");
        input.setIdempotencyKey("retry-key");

        DocumentRenewalPaymentResponse response =
                service.create("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", input);

        assertEquals("11111111-1111-1111-1111-111111111111", response.getTransactionId());
        assertEquals("260700001", response.getRequestNo());
        verify(omise).qrCodeDownloadUri(null);
        verifyNoMoreInteractions(omise);
        verify(payments, never()).insert(any());
    }

    @Test
    void rejectsMobileBankingWithoutReturnUri() {
        DocumentRenewalFoundationRepository foundation = mock(DocumentRenewalFoundationRepository.class);
        DocumentRenewalPaymentRepository payments = mock(DocumentRenewalPaymentRepository.class);
        OmisePaymentClient omise = mock(OmisePaymentClient.class);
        HttpServletRequest http = mock(HttpServletRequest.class);
        DocumentRenewalPaymentService service =
                new DocumentRenewalPaymentService(foundation, payments, omise, http);
        UsersEntity user = new UsersEntity();
        user.setMobileUuid("mobile-user-uuid");
        when(http.getAttribute("userObject")).thenReturn(user);
        DocumentRenewalRequestEntity request = new DocumentRenewalRequestEntity();
        request.setId("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        request.setStatusNameEn(DocumentRenewalStatus.PAYMENT_PENDING.getMasterNameEn());
        when(foundation.lockOwnedRequest("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", "mobile-user-uuid"))
                .thenReturn(request);
        DocumentRenewalPaymentRequest input = new DocumentRenewalPaymentRequest();
        input.setPaymentMethod("mobile_banking_kbank");
        input.setIdempotencyKey("renewal-260700001-kbank-1");

        assertThrows(RuntimeException.class,
                () -> service.create("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", input));
        verifyNoInteractions(omise);
        verify(payments, never()).insert(any());
    }

    @Test
    void rejectsPaymentWhenRequestScopedFilesAreIncomplete() {
        DocumentRenewalFoundationRepository foundation = mock(DocumentRenewalFoundationRepository.class);
        DocumentRenewalPaymentRepository payments = mock(DocumentRenewalPaymentRepository.class);
        OmisePaymentClient omise = mock(OmisePaymentClient.class);
        HttpServletRequest http = mock(HttpServletRequest.class);
        DocumentRenewalPaymentService service =
                new DocumentRenewalPaymentService(foundation, payments, omise, http);
        UsersEntity user = new UsersEntity();
        user.setMobileUuid("mobile-user-uuid");
        when(http.getAttribute("userObject")).thenReturn(user);
        DocumentRenewalRequestEntity request = new DocumentRenewalRequestEntity();
        request.setId("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        request.setStatusNameEn(DocumentRenewalStatus.PAYMENT_PENDING.getMasterNameEn());
        when(foundation.lockOwnedRequest("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", "mobile-user-uuid"))
                .thenReturn(request);
        when(foundation.countIncompleteRequestScopedItems("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"))
                .thenReturn(1);
        DocumentRenewalPaymentRequest input = new DocumentRenewalPaymentRequest();
        input.setPaymentMethod("promptpay");
        input.setIdempotencyKey("renewal-260700001-1");

        assertThrows(BusinessException.class,
                () -> service.create("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", input));
        verifyNoInteractions(payments, omise);
    }

    @Test
    void rejectsPaymentAttemptWhenRequestAlreadyAdvanced() {
        DocumentRenewalFoundationRepository foundation = mock(DocumentRenewalFoundationRepository.class);
        DocumentRenewalPaymentRepository payments = mock(DocumentRenewalPaymentRepository.class);
        OmisePaymentClient omise = mock(OmisePaymentClient.class);
        HttpServletRequest http = mock(HttpServletRequest.class);
        DocumentRenewalPaymentService service =
                new DocumentRenewalPaymentService(foundation, payments, omise, http);
        UsersEntity user = new UsersEntity();
        user.setMobileUuid("mobile-user-uuid");
        when(http.getAttribute("userObject")).thenReturn(user);
        DocumentRenewalRequestEntity request = new DocumentRenewalRequestEntity();
        request.setStatusNameEn(DocumentRenewalStatus.PENDING_DOCUMENT_REVIEW.getMasterNameEn());
        when(foundation.lockOwnedRequest("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", "mobile-user-uuid"))
                .thenReturn(request);
        DocumentRenewalPaymentRequest input = new DocumentRenewalPaymentRequest();
        input.setPaymentMethod("promptpay");
        input.setIdempotencyKey("renewal-260700001-1");

        assertThrows(RuntimeException.class,
                () -> service.create("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", input));
        verifyNoInteractions(payments, omise);
    }
}
