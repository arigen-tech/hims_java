package com.hims.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class IpMarDetailsResponse {
    private Long inpatientId;
    private LocalDateTime administrationTime;
    private Long itemId;
    private String nomenclature;
    private String routeName;
    private String dose;
    private BigDecimal administeredQty;
    private String batchNo;
    private LocalDate expiryDate;
    private String administeredBy;
    private String remarks;
}
