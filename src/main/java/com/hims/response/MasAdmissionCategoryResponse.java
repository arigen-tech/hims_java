package com.hims.response;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MasAdmissionCategoryResponse {

    private Long admissionCategoryId;
    private String admissionCategoryName;
    private String description;
    private String status;
    private LocalDateTime lastUpdateDate;
    private String createdBy;
    private String lastUpdatedBy;
}