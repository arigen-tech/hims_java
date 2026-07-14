package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TotalBedCountResponse {
    private Long totalBeds;
    private Long VacantBeds;
    private Long ReportedWard;
    private String wardName;
}
