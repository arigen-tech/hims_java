package com.hims.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StockStatusSummaryReportDTO {
    private String drugCode;
    private String drugName;
    private String au;
    private int stockQty;
    private String hospitalNameAddress;

    public StockStatusSummaryReportDTO(String drugCode, String drugName, String au, int stockQty, String hospitalNameAddress) {
        this.drugCode = drugCode;
        this.drugName = drugName;
        this.au = au;
        this.stockQty = stockQty;
        this.hospitalNameAddress = hospitalNameAddress;
    }
}
