package com.hims.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreationReq {
    private String email;
    private String currentPassword;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private Integer userFlag;
    private Long employeeId;
    private Long hospitalId;
    private Long userTypeId;
    private String userName;
}
