package com.hims.response;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class IpdConsultationTariffResponse {
    private Long tariffId;
    private Long serviceCategoryId;
    private String serviceCategoryName;
    private Long visitTypeId;
    private String visitTypeName;
    private Long departmentId;
    private String departmentName;
    private Long doctorId;
    private String doctorName;
    private BigDecimal baseTariff;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    private String status;

}