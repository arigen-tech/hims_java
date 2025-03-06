package com.hims.request;

import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Getter
@Setter
@NoArgsConstructor
@ToString
@AllArgsConstructor
@Builder
public class UserDetailsReq {

    private String email;
    private String mobileNo;
    private String phoneNumber;
    private String currentPassword;
    private String firstName;
    private String middleName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String nationality;
    private String addressInfo;
    private String pinCode;
    private Boolean isVerified;
    private String verificationMethod;
    private String panNumber;
    private String aadharNumber;
    private String passportNumber;
    private Instant passportExpiryDate;
    private String socialMediaProvider;
    private String socialMediaUserId;
    private String status;
    private String userName;
    private String profilePicture;
    private Long employeeId;
    private Long hospitalId;
    private Long userTypeId;
    private String roleId;
    private Integer userFlag;
}
