package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IndentDetailsResponseForRequestDept {
    private Long indentTId;
    private String itemName;
    private String itemUnitName;
    private BigDecimal qtyRequested;
    private BigDecimal qtyApproved;
    private BigDecimal qtyReceived;
    private  String reasonForIndent;
    private BigDecimal storeAvailableStock;
    private BigDecimal currentDeptAvailableStock;
}
