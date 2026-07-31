package com.hims.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IpDailyCaseSheetEntryRequest {

    @NotNull(message = "Inpatient ID is required")
    private Long inpatientId;
    @NotNull(message = "Doctor ID is required")
    private Long doctorId;
    private Long visitType;
    @NotNull(message = "Visit department ID is required")
    private Long visitDepartmentId;
    private String doctorNotes;
    private String investigationSummary;
    private String medicineSummary;
    private String procedureSummary;
    private String carePlanChanges;
    private String nextFollowUpPlan;


}