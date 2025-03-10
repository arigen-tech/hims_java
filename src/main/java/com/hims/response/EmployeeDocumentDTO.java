package com.hims.response;

import com.hims.entity.EmployeeDocument;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record EmployeeDocumentDTO(
        Long employeeDocumentId,
        String documentName,
        String documentEncryptName,
        String filePath,
        String lastChangedBy,
        LocalDateTime lastChangedDate
) {
    public static EmployeeDocumentDTO fromEntity(EmployeeDocument document) {
        return EmployeeDocumentDTO.builder()
                .employeeDocumentId(document.getEmployeeDocumentId())
                .documentName(document.getDocumentName())
                .documentEncryptName(document.getDocumentEncryptName())
                .filePath(document.getFilePath())
                .lastChangedBy(document.getLastChangedBy())
                .lastChangedDate(document.getLastChangedDate())
                .build();
    }
}
