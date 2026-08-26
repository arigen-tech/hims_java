package com.hims.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
@Data
public class CurrentActiveDietScheduleRequest {
    private Long inpatientId;
    private Long dietOrderId;
    private LocalDate dietDate;
    private Long dietMealId;
    private LocalTime planedTime;
    private Long scheduleStatusId;
    private BigDecimal Consumed;
    private String remark;
    private LocalTime actualTime;
    private String givenBy;



}
