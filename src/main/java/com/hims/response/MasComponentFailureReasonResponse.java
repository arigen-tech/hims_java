package com.hims.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MasComponentFailureReasonResponse {

    private Long failureReasonId;
    private String failureReasonCode;
    private String failureReasonName;
    private String description;
    private String status;
    private LocalDateTime lastUpdateDate;

}