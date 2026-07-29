package com.hims.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class IntakeOutputResponse {
    private Long inpatientId;
    private Long ioEntryId;
    private String ioType;
    private Long intakeTypeId;
    private String intakeTypeName;
    private Long intakeItemId;
    private String intakeItemName;
    private LocalDateTime dateTime;
    private BigDecimal quantity;
    private Long outputTypeId;
    private String outputName;

}
