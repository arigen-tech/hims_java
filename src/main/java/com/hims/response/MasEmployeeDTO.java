package com.hims.response;

import com.hims.entity.MasEmployee;
import lombok.Builder;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Builder
public record MasEmployeeDTO(
        Long employeeId,
        String firstName,
        String middleName,
        String lastName,
        LocalDate dob,
        String gender,
        String address1,
        String country,
        String state,
        String district,
        String city,
        String pincode,
        String mobileNo,
        String registrationNo,
        String identificationType,
//        String department,
        Instant fromDate,
        String profilePicName,
        String idDocumentName,
        String email,
        String status,
        List<EmployeeQualificationDTO> qualifications,
        List<EmployeeDocumentDTO> documents
) {
    public static MasEmployeeDTO fromEntity(
            MasEmployee employee,
            List<EmployeeQualificationDTO> qualifications,
            List<EmployeeDocumentDTO> documents
    ) {
        return MasEmployeeDTO.builder()
                .employeeId(employee.getEmployeeId())
                .firstName(employee.getFirstName())
                .middleName(employee.getMiddleName())
                .lastName(employee.getLastName())
                .dob(employee.getDob())
                .gender(employee.getGenderId() != null ? employee.getGenderId().getGenderName() : null)
                .address1(employee.getAddress1())
                .country(employee.getCountryId() != null ? employee.getCountryId().getCountryName() : null)
                .state(employee.getStateId() != null ? employee.getStateId().getStateName() : null)
                .district(employee.getDistrictId() != null ? employee.getDistrictId().getDistrictName() : null)
                .city(employee.getCity())
                .pincode(employee.getPincode())
                .mobileNo(employee.getMobileNo())
                .registrationNo(employee.getRegistrationNo())
                .identificationType(employee.getIdentificationType() != null ? employee.getIdentificationType().getIdentificationName() : null)
//                .department(employee.getDepartmentId() != null ? employee.getDepartmentId().getDepartmentName() : null)
                .fromDate(employee.getFromDate())
                .profilePicName(employee.getProfilePicName())
                .idDocumentName(employee.getIdDocumentName())
                .email(employee.getEmail())
                .status(employee.getStatus())
                .qualifications(qualifications)
                .documents(documents)
                .build();
    }
}
