package com.seaman.entity;


import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class UsersEntity extends CommonEntity implements Serializable {

    private String smartSeamanId;
    private String mobileUserId;
    private String userId;
    private String mobileUuid;
    private String groupId;
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private String companyCode;
    private String positionCode;
    private String email;
    private String mobileNumber;
    private String profilePicture;
    private String userStatus;
    private String lastLogin;
    private String dateOfBirth;
    private String displayName;
    private String displayType;
}
