package com.hims.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class StoreInternalIndentRequest {
    private Long indentMId;
    private LocalDateTime indentDate;
    private Long toDeptId;
    private String indentType;
    private List<Long> deletedT;

    private List<StoreInternalIndentDetailRequest> items;
}
