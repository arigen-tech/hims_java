package com.hims.jwt;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@ToString
@AllArgsConstructor
@Builder
public class OtpRequest {

    private String username;
    private String otp;
    private String role;
    private boolean isAgent;
}
