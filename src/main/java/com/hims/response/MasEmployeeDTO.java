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
        Long genderId,
        String gender,
        String address1,
        Long countryId,
        String country,
        Long stateId,
        String state,
        Long districtId,
        String district,
        String city,
        String pincode,
        String mobileNo,
        String registrationNo,
        Long identificationTypeId,//
        String identificationType,
        Long employmentTypeId,
        String employmentType,
        Long employeeTypeId,
        String employeeType,
        Long roleId,
        String role,
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
                .genderId(employee.getGenderId() != null ? employee.getGenderId().getId() : null)
                .gender(employee.getGenderId() != null ? employee.getGenderId().getGenderName() : null)
                .address1(employee.getAddress1())
                .countryId(employee.getCountryId() != null ? employee.getCountryId().getId() : null)
                .country(employee.getCountryId() != null ? employee.getCountryId().getCountryName() : null)
                .stateId(employee.getStateId() != null ? employee.getStateId().getId() : null)
                .state(employee.getStateId() != null ? employee.getStateId().getStateName() : null)
                .districtId(employee.getDistrictId() != null ? employee.getDistrictId().getId() : null)
                .district(employee.getDistrictId() != null ? employee.getDistrictId().getDistrictName() : null)
                .city(employee.getCity())
                .pincode(employee.getPincode())
                .mobileNo(employee.getMobileNo())
                .registrationNo(employee.getRegistrationNo())
                .identificationTypeId(employee.getIdentificationType() != null ? employee.getIdentificationType().getIdentificationTypeId() : null)
                .identificationType(employee.getIdentificationType() != null ? employee.getIdentificationType().getIdentificationName() : null)
                .employmentTypeId(employee.getEmploymentTypeId() != null ? employee.getEmploymentTypeId().getId() : null)
                .employmentType(employee.getEmploymentTypeId() != null ? employee.getEmploymentTypeId().getEmploymentType() : null)
                .employeeTypeId(employee.getEmployeeTypeId() != null ? employee.getEmployeeTypeId().getUserTypeId() : null)
                .employeeType(employee.getEmployeeTypeId() != null ? employee.getEmployeeTypeId().getUserTypeName() : null)
                .roleId(employee.getRoleId() != null ? employee.getRoleId().getId() : null)
                .role(employee.getRoleId() != null ? employee.getRoleId().getRoleDesc() : null)
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