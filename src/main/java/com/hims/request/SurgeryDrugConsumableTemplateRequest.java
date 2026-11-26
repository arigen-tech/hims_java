package com.hims.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SurgeryDrugConsumableTemplateRequest {
    @Positive
    private Long templateId;

    @NotNull @Positive
    private Long surgeryId;

    @NotBlank
    private String templateName;

    private String description;
    private String status;

    @Valid @NotEmpty
    private List<Item> items;

    @Data
    public static class Item {
        @NotNull @Positive
        private Long itemId;
        @NotNull @Positive
        private BigDecimal plannedQuantity;
    }
}
