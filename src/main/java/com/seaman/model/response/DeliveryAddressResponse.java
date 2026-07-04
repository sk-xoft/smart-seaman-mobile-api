package com.seaman.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeliveryAddressResponse {
    private String id;
    private String firstName;
    private String lastName;
    private String addressLine;
    private String province;
    private String district;
    private String subDistrict;
    private String postalCode;
    private Boolean isDefault;
}
