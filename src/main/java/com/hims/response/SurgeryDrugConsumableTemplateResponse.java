package com.hims.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurgeryDrugConsumableTemplateResponse {
    private Long templateId;
    private Long surgeryId;
    private String surgeryName;
    private String templateName;
    private String description;
    private String sectionCode;
    private String status;
    private String lastChgBy;
    private LocalDateTime lastChgDate;
    private List<Item> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private Long templateDetailId;
        private Long itemId;
        private String itemName;
        private String pvmsNo;
        private Integer sectionId;
        private String sectionCode;
        private String sectionName;
        private BigDecimal plannedQuantity;
        private Long unitId;
        private String unitName;
        private String status;
    }
}
