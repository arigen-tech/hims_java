package com.hims.response;
import com.hims.entity.MasEmployeeCenterMapping;
import lombok.Builder;

@Builder
public record EmployeeSpecialtyCenterMappingDTO(
        Long empId,
        Long centerId,
        Boolean isPrimary
) {
    public static EmployeeSpecialtyCenterMappingDTO fromEntity(MasEmployeeCenterMapping masEmployeeCenterMapping) {
        return EmployeeSpecialtyCenterMappingDTO.builder()
                .empId(masEmployeeCenterMapping.getEmpId())
                .centerId(masEmployeeCenterMapping.getCenterId())
                .isPrimary(masEmployeeCenterMapping.getIsPrimary())
                .build();

    }
}
