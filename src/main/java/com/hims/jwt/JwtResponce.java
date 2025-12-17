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
    private long userId;
    private String roleId;
    private long jwtTokenExpiry;
    private long refreshTokenExpiry;
    private long hospitalId;
    private long departmentId;
    private String departmentName;

}
