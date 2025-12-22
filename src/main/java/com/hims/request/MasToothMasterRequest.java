package com.hims.request;

import lombok.Data;

@Data
public class MasToothMasterRequest {

    private String toothNumber;
    private String toothType;
    private Integer quadrant;
    private Integer displayOrder;
}