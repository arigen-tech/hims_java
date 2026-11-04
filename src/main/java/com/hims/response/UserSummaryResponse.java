package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserSummaryResponse {
    private Long userId;
    private String fullName;
    private String status;
    private Long userTypeId;
    private String userTypeName;
}
