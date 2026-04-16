package com.hims.request;

import lombok.Data;

import java.time.LocalDate;
@Data
public class LabRadioInvestigationRequest {
    private Long id;
    private LocalDate appointmentDate;
    private Boolean checkStatus;
    private int actualAmount;
    private int discountedAmount;
    private String type;

}
