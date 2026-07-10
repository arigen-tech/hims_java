package com.hims.request;

import lombok.Data;

@Data
public class RadiologyReportRequest {
    private Long radOrderDtId;
    private String reportDesc;
}
