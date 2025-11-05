package com.hims.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class InvestigationForTemplateRequest {
    private Long investigationId;
    private LocalDate DateOfOrder;
}
