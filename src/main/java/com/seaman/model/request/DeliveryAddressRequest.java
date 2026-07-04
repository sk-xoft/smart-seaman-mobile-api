package com.seaman.model.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@Setter
public class DeliveryAddressRequest {
    @NotBlank(message = "firstName")
    @Size(max = 100, message = "firstName")
    private String firstName;

    @NotBlank(message = "lastName")
    @Size(max = 100, message = "lastName")
    private String lastName;

    @NotBlank(message = "addressLine")
    @Size(max = 500, message = "addressLine")
    private String addressLine;

    @NotBlank(message = "province")
    @Size(max = 100, message = "province")
    private String province;

    @NotBlank(message = "district")
    @Size(max = 100, message = "district")
    private String district;

    @NotBlank(message = "subDistrict")
    @Size(max = 100, message = "subDistrict")
    private String subDistrict;

    @NotBlank(message = "postalCode")
    @Pattern(regexp = "^[0-9]{5}$", message = "postalCode")
    private String postalCode;

    @NotNull(message = "isDefault")
    private Boolean isDefault;
}
