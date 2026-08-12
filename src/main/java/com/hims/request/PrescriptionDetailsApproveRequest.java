package com.hims.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PrescriptionDetailsApproveRequest {

    private Long prescriptionDetailsId;
    private Long itemId;
    private Long stockId;
    private String batchName;
    private String dosage;
    private String frequency;
    private Integer days;
    private BigDecimal total;
    private BigDecimal issuedQty;
    private String instruction;


}
