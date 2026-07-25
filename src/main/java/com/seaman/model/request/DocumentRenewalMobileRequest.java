package com.seaman.model.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
@Setter
public class DocumentRenewalMobileRequest {
    @NotBlank(message = "mobileNumber")
    @Pattern(regexp = "^0[0-9]{9}$", message = "mobileNumber")
    private String mobileNumber;
}
