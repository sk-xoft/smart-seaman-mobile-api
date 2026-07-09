package com.seaman.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seaman.constant.DocumentRenewalAction;
import com.seaman.constant.DocumentRenewalStatus;
import com.seaman.entity.DocumentRenewalRequestEntity;
import com.seaman.entity.PaymentTransactionEntity;
import com.seaman.event.DocumentRenewalPaymentSucceededEvent;
import com.seaman.model.external.response.OmiseChargeResponse;
import com.seaman.repository.DocumentRenewalFoundationRepository;
import com.seaman.repository.DocumentRenewalPaymentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class OmiseWebhookServiceTest {

    @Test
    void successfulChargeCompleteTransitionsRequestOnce() throws Exception {
        OmisePaymentClient omise = mock(OmisePaymentClient.class);
        DocumentRenewalPaymentRepository payments = mock(DocumentRenewalPaymentRepository.class);
        DocumentRenewalFoundationRepository foundation = mock(DocumentRenewalFoundationRepository.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        Environment environment = mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(new String[0]);
        DocumentRenewalPaymentService paymentService =
                new DocumentRenewalPaymentService(null, null, null, null);
        OmiseWebhookService service = new OmiseWebhookService(
                new ObjectMapper(), omise, paymentService, payments, foundation, publisher, environment);
        OmiseChargeResponse charge = new OmiseChargeResponse();
        charge.setId("chrg_test_1");
        charge.setStatus("successful");
        charge.setRaw(new ObjectMapper().readTree("{\"id\":\"chrg_test_1\",\"status\":\"successful\"}"));
        when(omise.retrieveCharge("chrg_test_1")).thenReturn(charge);
        when(omise.rawJson(charge.getRaw())).thenReturn("{\"id\":\"chrg_test_1\"}");
        PaymentTransactionEntity payment = new PaymentTransactionEntity();
        payment.setId("payment-id");
        payment.setRequestId("request-id");
        when(payments.lockByProviderChargeId("chrg_test_1")).thenReturn(payment);
        DocumentRenewalRequestEntity request = new DocumentRenewalRequestEntity();
        request.setId("request-id");
        request.setRequestNo("260700001");
        request.setMobileUserUuid("mobile-user-uuid");
        request.setDocumentStatusId("payment-status-id");
        request.setStatusNameEn(DocumentRenewalStatus.PAYMENT_PENDING.getMasterNameEn());
        when(foundation.lockRequest("request-id")).thenReturn(request);
        when(foundation.findActiveStatusId(DocumentRenewalStatus.PENDING_DOCUMENT_REVIEW))
                .thenReturn("review-status-id");

        service.handle("{\"key\":\"charge.complete\",\"data\":{\"id\":\"chrg_test_1\"}}",
                null, null);

        verify(payments).updateFromProvider(argThat(updated -> "SUCCESS".equals(updated.getStatus())));
        verify(foundation).updateStatus("request-id", "payment-status-id", "review-status-id");
        verify(foundation).appendTransaction("request-id", DocumentRenewalAction.PAYMENT_SUCCESS,
                DocumentRenewalStatus.PAYMENT_PENDING,
                DocumentRenewalStatus.PENDING_DOCUMENT_REVIEW,
                "Payment succeeded by Omise webhook", null);
        verify(publisher).publishEvent(any(DocumentRenewalPaymentSucceededEvent.class));
    }

    @Test
    void duplicateSuccessfulWebhookOnlyUpdatesPaymentWhenRequestAlreadyAdvanced() throws Exception {
        OmisePaymentClient omise = mock(OmisePaymentClient.class);
        DocumentRenewalPaymentRepository payments = mock(DocumentRenewalPaymentRepository.class);
        DocumentRenewalFoundationRepository foundation = mock(DocumentRenewalFoundationRepository.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        Environment environment = mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(new String[0]);
        DocumentRenewalPaymentService paymentService =
                new DocumentRenewalPaymentService(null, null, null, null);
        OmiseWebhookService service = new OmiseWebhookService(
                new ObjectMapper(), omise, paymentService, payments, foundation, publisher, environment);
        OmiseChargeResponse charge = new OmiseChargeResponse();
        charge.setId("chrg_test_1");
        charge.setStatus("successful");
        charge.setRaw(new ObjectMapper().readTree("{\"id\":\"chrg_test_1\",\"status\":\"successful\"}"));
        when(omise.retrieveCharge("chrg_test_1")).thenReturn(charge);
        when(omise.rawJson(charge.getRaw())).thenReturn("{\"id\":\"chrg_test_1\"}");
        PaymentTransactionEntity payment = new PaymentTransactionEntity();
        payment.setId("payment-id");
        payment.setRequestId("request-id");
        when(payments.lockByProviderChargeId("chrg_test_1")).thenReturn(payment);
        DocumentRenewalRequestEntity request = new DocumentRenewalRequestEntity();
        request.setId("request-id");
        request.setStatusNameEn(DocumentRenewalStatus.PENDING_DOCUMENT_REVIEW.getMasterNameEn());
        when(foundation.lockRequest("request-id")).thenReturn(request);

        service.handle("{\"key\":\"charge.complete\",\"data\":{\"id\":\"chrg_test_1\"}}",
                null, null);

        verify(payments).updateFromProvider(argThat(updated -> "SUCCESS".equals(updated.getStatus())));
        verify(foundation, never()).findActiveStatusId(any());
        verify(foundation, never()).updateStatus(anyString(), anyString(), anyString());
        verify(foundation, never()).appendTransaction(anyString(), any(), any(), any(), anyString(), any());
        verifyNoInteractions(publisher);
    }

    @Test
    void missingWebhookSecretFailsClosedInProd() {
        OmisePaymentClient omise = mock(OmisePaymentClient.class);
        DocumentRenewalPaymentRepository payments = mock(DocumentRenewalPaymentRepository.class);
        DocumentRenewalFoundationRepository foundation = mock(DocumentRenewalFoundationRepository.class);
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        Environment environment = mock(Environment.class);
        when(environment.getActiveProfiles()).thenReturn(new String[]{"prod"});
        DocumentRenewalPaymentService paymentService =
                new DocumentRenewalPaymentService(null, null, null, null);
        OmiseWebhookService service = new OmiseWebhookService(
                new ObjectMapper(), omise, paymentService, payments, foundation, publisher, environment);

        assertThrows(RuntimeException.class, () -> service.handle(
                "{\"key\":\"charge.complete\",\"data\":{\"id\":\"chrg_test_1\"}}",
                null, null));

        verifyNoInteractions(omise, payments, foundation, publisher);
    }
}
