package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserContext {

    private Long userId;
    private String userName;
    private String email;
    private Long hospitalId;
    private Long departmentId;
    private String userFullName;

}