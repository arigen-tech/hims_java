package com.hims.request;

import lombok.Data;

@Data
public class MasSurgeryTypeRequest {

    private String surgeryTypeCode;
    private String surgeryTypeName;
    private String description;
}