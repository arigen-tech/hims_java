package com.hims.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MasOtChecklistResponse {
    private Long checklistId;
    private String checklistStage;
    private String checklistCode;
    private String checklistName;
    private String isMandatory;
    private String allowNa;
    private Integer displayOrder;
    private String status;
    private String lastChgBy;
    private LocalDateTime lastChgDate;
}
