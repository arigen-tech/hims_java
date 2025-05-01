package com.hims.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class DgInvestigationPackageDTO {
    private Long packId;
    private String packName;
    private String descrp;
    private double baseCost;
    private double disc;
    private double discPer;
    private double actualCost;
    private String status;
    private String createdBy;
    private LocalDateTime createdDt;
    private String updatedBy;
    private LocalDateTime updatedDt;
    private LocalDate fromDt;
    private LocalDate toDt;
    private String category;
    private String discFlag;
}
