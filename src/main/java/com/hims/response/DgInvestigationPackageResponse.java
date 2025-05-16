package com.hims.response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
@Getter
@Setter
@Data
public class DgInvestigationPackageResponse {
    private Long packId;
    private String packName;
    private double actualCost;
    private String category;




    public DgInvestigationPackageResponse(Long packId, String packName, double actualCost, String category) {
        this.packId = packId;
        this.packName = packName;
        this.actualCost = actualCost;
        this.category = category;
    }
}
