package com.seaman.entity;

import lombok.Data;

@Data
public class ForgotPasswordEntity {
    private String userUuid;
    private String isStatus;
    private String createdAt;
}
