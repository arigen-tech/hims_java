package com.hims.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OtSurgeryDrugRequest {
    @NotNull @Positive
    private Long otBookingId;

    private Long otBookingRequestDtId;

    @NotEmpty
    private List<@Valid DrugItem> items;

    @Data
    public static class DrugItem {
        @NotNull @Positive
        private Long itemId;

        @NotNull @Positive
        private BigDecimal quantity;

        private String batchNo;
        private String remarks;
    }
}
