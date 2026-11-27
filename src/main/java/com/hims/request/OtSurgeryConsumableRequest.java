package com.hims.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OtSurgeryConsumableRequest {
    @NotNull @Positive
    private Long otBookingId;

    private Long otBookingRequestDtId;

    @NotEmpty
    private List<@Valid ConsumableItem> items;

    @Data
    public static class ConsumableItem {
        @NotNull @Positive
        private Long itemId;

        @NotNull @Positive
        private BigDecimal quantity;

        @PositiveOrZero
        private BigDecimal plannedQuantity;

        private String batchNo;
        private String remarks;
    }
}
