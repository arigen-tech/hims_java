package com.hims.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DietOrderRequest {
    private Long InpatientId;
    private Long dietTypeId;
    private String specialInstruction;
    private LocalDate effectiveFrom;
    private Long orderedBy;
    private String remark;
}
