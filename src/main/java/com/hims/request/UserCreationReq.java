package com.hims.request;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@ToString
@AllArgsConstructor
@Builder
public class UserCreationReq {
    private String email;
    private String curPassword;
    private String mobileNo;
    private String firstName;
    private String middleName;
    private String lastName;
    private String rollCode;
//    private Long genderid;
}
