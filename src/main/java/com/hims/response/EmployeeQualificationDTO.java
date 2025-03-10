package com.hims.response;

import com.hims.entity.EmployeeQualification;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record EmployeeQualificationDTO(
        Long employeeQualificationId,
        String institutionName,
        Integer completionYear,
        String documentName,
        String documentEncryptName,
        String qualificationName,
        String filePath,
        String lastChangedBy,
        LocalDateTime lastChangedDate
) {
    public static EmployeeQualificationDTO fromEntity(EmployeeQualification qualification) {
        return EmployeeQualificationDTO.builder()
                .employeeQualificationId(qualification.getEmployeeQualificationId())
                .institutionName(qualification.getInstitutionName())
                .completionYear(qualification.getCompletionYear())
                .documentName(qualification.getDocumentName())
                .documentEncryptName(qualification.getDocumentEncryptName())
                .qualificationName(qualification.getQualificationName())
                .filePath(qualification.getFilePath())
                .lastChangedBy(qualification.getLastChangedBy())
                .lastChangedDate(qualification.getLastChangedDate())
                .build();
    }
}
