package com.hims.response;

import com.hims.entity.MasUserType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
