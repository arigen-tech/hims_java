package com.hims.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for patient search results.
 * Contains only essential fields needed for search display.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientSearchResponseDTO {
    private Long patientId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String mobileNumber;
    private String uhidNo;
    private LocalDate dateOfBirth;
    private String gender;
    private Integer age;
}
