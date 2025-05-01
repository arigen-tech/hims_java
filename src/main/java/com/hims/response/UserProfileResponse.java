package com.hims.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileResponse {

    private String firstName;
    private String middleName;
    private String lastName;
    private String userName;
    private String profilePicture;
    private String rolesName;
}
