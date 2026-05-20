package com.hims.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class OpdHolidayMasterResponse {

    private Long opdHolidayId;

    private LocalDate holidayDate;

    private String holidayName;

    private String remarks;

    private String status;

    private String createdBy;

    private LocalDateTime createdAt;

    private String updatedBy;

    private LocalDateTime lastUpdatedDt;
}