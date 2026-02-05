package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class IndentTResponseForIndentTracking {

    private Long indentTId;
    private Long itemId;
    private String itemName;
    private Long itemUnitId;
    private String itemUnitName;
    private BigDecimal qtyRequested;
    private BigDecimal qtyApproved;
    private BigDecimal qtyReceived;
    private  String reasonForIndent;


}
