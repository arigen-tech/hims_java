package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrentActiveDietScheduleResponse {
    private Long inpatientId;
    private Long dietScheduleId;
    private LocalDate Date;
    private LocalTime planedTime;
    private LocalTime actualTime;
    private Long mealTypeId;
    private String mealType;
    private Long statusId;
    private String status;
    private BigDecimal consumed;
    private String remark;
    private String givenBy;

}
