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
public class PrescriptionDetailRequest {

        private Long itemId;
        private String dispUnit;
        private String dosage;
        private Long frequencyId;
        private Integer days;
        private Integer prescribedQty;
        private Long stockId;
        private String instruction;
        private BigDecimal issuedQty;

}
