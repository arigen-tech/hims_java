package com.hims.mapper;

import java.math.BigDecimal;

public record TreatmentData(
        Long prescrptionHdId,
        Long prescriptionDtId,
        Long itemId,
        String dosage,
        String frequency,
        Integer days,
        BigDecimal total,
        String instruction,
        Integer flag
) {
}