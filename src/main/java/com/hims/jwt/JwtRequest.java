package com.hims.jwt;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@ToString
@AllArgsConstructor
@Builder
public class JwtRequest {

    private String username;
    private String password;
    private Long departmentId;
}
