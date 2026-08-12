package com.hims.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class NursingCareProcedure {
    private Long itemId;
    private String itemName;
    private BigDecimal qty;
    private Long procedureTxnId;
    private String procedureName;
    private LocalDateTime dateTime;
    private String usedBy;
    private String batchNo;
    private LocalDate expiryDate;
    private String remark;


}
