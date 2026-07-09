package com.seaman.model;

import com.seaman.model.request.DocumentRenewalPaymentRequest;
import org.junit.jupiter.api.Test;

import javax.validation.Validation;
import javax.validation.Validator;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentRenewalPaymentRequestValidationTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void acceptsPromptPayAndMobileBankingMethods() {
        DocumentRenewalPaymentRequest promptPay = valid("promptpay");
        DocumentRenewalPaymentRequest kbank = valid("mobile_banking_kbank");
        kbank.setPlatformType("IOS");
        kbank.setReturnUri("smartseaman://document-renewals/payment-return");

        assertTrue(validator.validate(promptPay).isEmpty());
        assertTrue(validator.validate(kbank).isEmpty());
    }

    @Test
    void rejectsUnknownMethodUnsafeIdempotencyKeyAndPlatform() {
        DocumentRenewalPaymentRequest request = valid("internet_banking_scb");
        request.setIdempotencyKey("bad key with space");
        request.setPlatformType("WEB");

        assertFalse(validator.validate(request).isEmpty());
    }

    private DocumentRenewalPaymentRequest valid(String method) {
        DocumentRenewalPaymentRequest request = new DocumentRenewalPaymentRequest();
        request.setPaymentMethod(method);
        request.setIdempotencyKey("renewal-260700001-attempt-1");
        return request;
    }
}
