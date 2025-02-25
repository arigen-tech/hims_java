package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@NoArgsConstructor
@ToString
@AllArgsConstructor
public class UserDetailsResp {
    private String name;
    private String email;
    private String username;
    private String curPassword;
    private List<RoleInfoResp> role;
}
