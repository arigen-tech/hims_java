package com.hims.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IpAdverseEventRequest {

    @NotNull(message = "Inpatient ID is required")
    private Long inpatientId;

    private Long medicationId;

    @NotBlank(message = "Reaction is required")
    private String reaction;

    @NotBlank(message = "Severity is required")
    @Size(max = 20, message = "Severity must not exceed 20 characters")
    private String severity;

    private String actionTaken;

    @NotNull(message = "Reaction datetime is required")
    private LocalDateTime reactionDatetime;

    @Pattern(regexp = "^[YN]$", message = "Medication stopped must be 'Y' or 'N'")
    private String medicationStopped;
    @Pattern(regexp = "^[YN]$", message = "Doctor informed must be 'Y' or 'N'")
    private String doctorInformed;
    private Long informedDoctorId;
    private String patientConditionAfter;


}