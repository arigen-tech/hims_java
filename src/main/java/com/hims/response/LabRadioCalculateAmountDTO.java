package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class LabRadioCalculateAmountDTO {
    private BigDecimal total;
    private BigDecimal discount;
    private BigDecimal tax;
}
