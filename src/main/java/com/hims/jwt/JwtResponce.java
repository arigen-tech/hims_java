package com.hims.jwt;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@ToString
@AllArgsConstructor
@Builder
public class JwtResponce {

    private String jwtToken;
    private String refreshToken;
    private String username;
    private String roleId;
    private long jwtTokenExpiry;
    private long refreshTokenExpiry;

}
