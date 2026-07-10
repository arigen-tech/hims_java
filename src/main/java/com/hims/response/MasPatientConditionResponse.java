package com.hims.response;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MasPatientConditionResponse {

    private Long patientConditionId;

    private String patientConditionName;

    private String description;

    private String status;

    private LocalDateTime lastUpdateDate;

    private String createdBy;

    private String lastUpdatedBy;
}