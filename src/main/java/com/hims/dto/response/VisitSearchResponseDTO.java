package com.hims.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for visit search results.
 * Contains only essential fields needed for visit display.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitSearchResponseDTO {
    private Long visitId;
    private Long tokenNo;
    private LocalDateTime visitDate;
    private String visitStatus;
    private String billingStatus;
    private Long patientId;
    private String patientName;
    private String patientMobile;
    private Long doctorId;
    private String doctorName;
    private Long departmentId;
    private String departmentName;
}
