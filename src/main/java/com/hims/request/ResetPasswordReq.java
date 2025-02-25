package com.hims.request;

import lombok.*;

@Data
@NoArgsConstructor
@ToString
@AllArgsConstructor
@Builder
public class ResetPasswordReq {
    private String username;
    private String otp;
    private String newPassword;
    private String reEnterPassword;
}
