package com.hims.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BloodDonorScreeningRequest {
   // @NotNull(message = "Hemoglobin is required")
//    @DecimalMin(value = "5.0", message = "Hemoglobin must be >= 5")
//    @DecimalMax(value = "25.0", message = "Hemoglobin must be <= 25")
    private BigDecimal hemoglobin;

//    @NotNull(message = "Weight is required")
//    @DecimalMin(value = "30.0", message = "Weight must be >= 30")
//    @DecimalMax(value = "200.0", message = "Weight must be <= 200")
    private BigDecimal weightKg;

//    @NotNull(message = "Height is required")
//    @DecimalMin(value = "100.0", message = "Height must be >= 100")
//    @DecimalMax(value = "250.0", message = "Height must be <= 250")
    private BigDecimal heightCm;

//    @NotBlank(message = "Blood Pressure is required")
//    @Pattern(regexp = "^\\d{2,3}/\\d{2,3}$", message = "Invalid BP (e.g. 120/80)")
    private String bloodPressure;

//    @NotNull(message = "Pulse is required")
//    @Min(value = 30, message = "Pulse must be >= 30")
//    @Max(value = 200, message = "Pulse must be <= 200")
    private Integer pulseRate;

//    @NotNull(message = "Temperature is required")
//    @DecimalMin(value = "30.0", message = "Temp must be >= 30")
//    @DecimalMax(value = "45.0", message = "Temp must be <= 45")
    private BigDecimal temperature;

//    @NotBlank(message = "Screening result is required")
    private String screeningResult;

    private String deferralType;
    private String deferralReason;
}

