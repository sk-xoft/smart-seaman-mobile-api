package com.seaman.model.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@Setter
public class DocumentRequestValidateRequest {
    @NotBlank(message = "documentCode")
    @Size(max = 10, message = "documentCode")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "documentCode")
    private String documentCode;

    @Size(max = 100, message = "idempotencyKey")
    @Pattern(regexp = "^[A-Za-z0-9._:-]+$", message = "idempotencyKey")
    private String idempotencyKey;
}
