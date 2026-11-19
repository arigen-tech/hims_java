package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DentalProcedureHistoryRow {

    private Long procedureDtId;
    private Long procedureHdId;
    private String procedureName;
    private  String advisedBy;
    private LocalDateTime advisedDate;
    private Integer plannedSittings;
    private Integer completedSittings;
    private String teethNumber;
    private String status;
    private LocalDateTime firstSittingDate;
}