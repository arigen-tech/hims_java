package com.hims.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OtChecklistSubmitResponse {
    private Long otChecklistId;
    private Long otBookingId;
    private String checklistStage;
    private String checklistStatus;
    private String remarks;
    private String verifiedBy;
    private LocalDateTime verifiedDatetime;
    private String status;
    private String lastChgBy;
    private LocalDateTime lastChgDate;
    private List<ItemResponse> items;

    @Data
    public static class ItemResponse {
        private Long otChecklistItemId;
        private Long checklistId;
        private String checklistCode;
        private String checklistName;
        private String checklistStage;
        private String isMandatory;
        private String allowNa;
        private Integer displayOrder;
        private String verificationStatus;
        private String remarks;
        private String verifiedBy;
        private LocalDateTime verifiedDatetime;
    }
}
