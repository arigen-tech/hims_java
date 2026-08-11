package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class PatientPrescriptionDetailsResponse {

        private Long prescriptionHeaderId;
        private Long prescriptionDetailsId;
        private Long itemId;
        private String itemName;
        private String dosage;
        private String frequency;
        private Integer days;
        private BigDecimal prescribedQty;
        private BigDecimal issueQty;
        private String status;

}
