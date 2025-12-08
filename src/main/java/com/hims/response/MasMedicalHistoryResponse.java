package com.hims.response;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MasMedicalHistoryResponse {
    private Long medicalHistoryId;
    private String medicalHistoryName;
    private String status ;
    private LocalDateTime lastUpdateDate;
    private String createdBy;
    private String lastUpdatedBy;
}
