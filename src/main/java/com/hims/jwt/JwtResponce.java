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
    private String role;
//    private UserRole role;
//    private ArrayList<RoleInfoResp> role;

}
