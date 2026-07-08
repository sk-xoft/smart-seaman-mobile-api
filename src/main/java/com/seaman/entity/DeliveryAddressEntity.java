package com.seaman.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class DeliveryAddressEntity {
    private String id;
    private String mobileUserUuid;
    private String firstName;
    private String lastName;
    private String addressLine;
    private String province;
    private String district;
    private String subDistrict;
    private String postalCode;
    private String mobileNumber;
    private Boolean isDefault;
    private String isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
