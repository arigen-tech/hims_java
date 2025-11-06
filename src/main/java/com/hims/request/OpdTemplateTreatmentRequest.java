package com.hims.request;

import lombok.Data;

@Data
public class OpdTemplateTreatmentRequest {
    private String dosage;
    private Long noOfDays;   // ✅ Changed to Long
    private Long total;
    private String instruction;
    private Long frequencyId;
    private Long itemId;
}

