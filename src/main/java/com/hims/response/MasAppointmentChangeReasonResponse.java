package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MasAppointmentChangeReasonResponse {

    private Long reasonId;
    private String reasonCode;
    private String reasonName;
    private String status;
    private Instant lastUpdateDate;
    private String createdBy;
    private String lastUpdatedBy;

}