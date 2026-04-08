package com.hims.request;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class IpdConsultationTariffRequest {

    private Long serviceCategoryId;
    private Long visitTypeId;
    private Long hospitalId;
    private Long departmentId;
    private Long doctorId;
    private BigDecimal baseTariff;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
}