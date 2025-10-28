package com.hims.request;

import com.hims.entity.MasStoreItem;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class IndentTRequest {
    private Long itemId;
    private BigDecimal requestedQty;
}
