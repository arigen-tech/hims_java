package com.hims.response;

import com.hims.entity.MasEmployeeLanguageMapping;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class EmployeeLanguageDTO {
    private Long employeeId;
    private Long languageId;

    public static EmployeeLanguageDTO fromEntity(MasEmployeeLanguageMapping mapping) {
        return EmployeeLanguageDTO.builder()
                .employeeId(mapping.getEmpId())
                .languageId(mapping.getLanguageId())
                .build();
    }
}