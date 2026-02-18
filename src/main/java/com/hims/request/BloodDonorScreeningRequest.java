package com.hims.request;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BloodDonorScreeningRequest {
    private LocalDate screeningDate;
    private BigDecimal hemoglobin;
    private BigDecimal weightKg;
    private BigDecimal heightCm;
    private String bloodPressure;
    private Integer pulseRate;
    private BigDecimal temperature;
    private String screeningResult;
    private String deferralType;
    private String deferralReason;
    private LocalDate deferralUptoDate;
    private String remarks;
}

