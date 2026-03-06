package com.seaman.model.request;

import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class ProfileRequest {

    @NotBlank(message = "First name")
    private String firstName;

    @NotBlank(message =  "Last name")
    private String lastName;

    private String companyCode;

    private String dateOfBirth;

    @NotBlank(message =  "Position")
    private String positionCode;

    @NotBlank(message =  "Email")
    private String email;

    private String mobileNumber;

    private String isChangeFile;

    private String imageProfile;
}