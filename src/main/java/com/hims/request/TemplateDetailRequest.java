package com.hims.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplateDetailRequest {

    @NotNull(message = "item_id is required")
    private Long itemId;

    @NotNull(message = "default_qty is required")
    @DecimalMin(value = "0.01", message = "default_qty must be greater than 0")
    private BigDecimal defaultQty;

}