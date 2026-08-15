package com.hims.mapper;

import java.time.LocalDate;

public record InvestigationData(
        Long id,
        String investigationName,
        LocalDate investigationDate,
        Long investigationId,
        Integer flag
) {
}
