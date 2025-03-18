package com.hims.response;

import com.hims.entity.MasUserType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponse {
    Long userId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String RoleId;
    private String userName;
    private String email;
    private String mobileNo;
    private String status;
    private MasUserType userType;


}
