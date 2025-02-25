package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
@AllArgsConstructor
public class RoleInfoResp {
    private String userFullName;
    private String username;
    private String roleCode;
    private String roleDesc;
    private String status;
}
