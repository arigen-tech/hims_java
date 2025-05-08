package com.hims.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DgInvestigationPackageRequest {
    private String packName;
    private String descrp;
    private double baseCost;
    private double disc;
    private double discPer;
    private double actualCost;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate fromDt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate toDt;

    private String category;
    private String discFlag;
}
