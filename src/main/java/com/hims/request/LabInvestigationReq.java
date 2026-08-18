package com.hims.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
public class LabInvestigationReq {
    private Long id;
    private LocalDate appointmentDate;
   // private boolean checkStatus;
    private int actualAmount;
    private int discountedAmount;
    private String type;


}
