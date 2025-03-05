package com.hims.request;

import lombok.*;

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
    private String getPhoneNumber;
    private String getCurrentPassword;
    private String firstName;
    private String middleName;
    private String lastName;
    private String gender;
    private LocalDate dob;
    private String nationality;
    private String address;
    private String pinCode;
    private Integer state;
    private String profilePic;
    private Boolean isVerify;
    private String verifyMethod;
    private String panNo;
    private String aadhaarNo;
    private String passportNo;
    private String passportIssueAuthority;
    private LocalDate passportExpiryDate;
    private String socialMediaUserId;
    private String socialMediaProvider;
}
