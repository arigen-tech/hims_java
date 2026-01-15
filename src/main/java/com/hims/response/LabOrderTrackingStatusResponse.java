package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class LabOrderTrackingStatusResponse {

    private Long orderStatusId;
    private  String orderStatusCode;
    private String orderStatusName;
    private String description;
    private String status;
    private LocalDateTime updateDate;
}
