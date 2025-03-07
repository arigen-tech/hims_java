package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
public class MasBloodGroupResponse {
    private Long bloodGroupId;
    private String bloodGroupCode;
    private String bloodGroupName;
    private String status;
    private String lastChangedBy;
    private Instant lastChangedDate;
    private String lastChangedTime;
    private String hicCode;
}
