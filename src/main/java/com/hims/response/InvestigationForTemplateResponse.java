package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class InvestigationForTemplateResponse {
    private int dgOrderHdId;
    private Long investigationId;
    private String investigationName;
    private LocalDate DateOfOrder;
    private int dgOrderDtId;
}
