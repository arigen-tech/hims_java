package com.hims.request;

import lombok.Data;

@Data
public class MasOutputTypeRequest {
    private String outputTypeName;
    private String isMeasurable;
    private String description;
}
