package com.hims.response;

import lombok.Data;

import java.util.List;

@Data
public class InvestigationPackageDTO {
    private Long packageId;
    private String packName;
    private double actualCost;
    private String status;

    private List<DgMasInvestigationRes> investigations;
}
