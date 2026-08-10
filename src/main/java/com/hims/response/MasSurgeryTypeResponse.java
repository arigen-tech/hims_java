package com.hims.response;

import lombok.Data;

@Data
public class MasSurgeryTypeResponse {

    private Long surgeryTypeId;
    private String surgeryTypeCode;
    private String surgeryTypeName;
    private String description;
    private String status;
}