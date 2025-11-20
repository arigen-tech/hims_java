package com.hims.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class StoreInternalIndentRequest {
    private Long indentMId;            // null for create, present for update
    private String indentNo;           // optional
    private LocalDateTime indentDate;  // optional, defaulted when null
    private Long toDeptId;             // requested department id
    private BigDecimal totalCost;      // optional - will be recalculated
    private String sourceType;         // "M"=Manual, "R"=ROL, "P"=Previous (optional)

    private List<StoreInternalIndentDetailRequest> items;
}
