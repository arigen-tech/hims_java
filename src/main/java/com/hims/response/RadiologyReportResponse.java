package com.hims.response;

import lombok.Data;

@Data
public class RadiologyReportResponse {
    private Long radStudyReportId;
    private Long radOrderDtId;
    private String reportDesc;
    private String reportImagePath;
    private String reportStatus;
}
