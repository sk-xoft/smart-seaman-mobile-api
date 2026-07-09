package com.seaman.model.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@Setter
public class DocumentRenewalPaymentRequest {
    @NotBlank(message = "paymentMethod")
    @Pattern(regexp = "^(promptpay|mobile_banking_bbl|mobile_banking_kbank|mobile_banking_ktb|mobile_banking_bay|mobile_banking_scb)$",
            message = "paymentMethod")
    private String paymentMethod;

    @NotBlank(message = "idempotencyKey")
    @Size(max = 100, message = "idempotencyKey")
    @Pattern(regexp = "^[A-Za-z0-9._:-]+$", message = "idempotencyKey")
    private String idempotencyKey;

    @Size(max = 500, message = "returnUri")
    private String returnUri;

    @Pattern(regexp = "^(IOS|ANDROID)$", message = "platformType")
    private String platformType;
}
