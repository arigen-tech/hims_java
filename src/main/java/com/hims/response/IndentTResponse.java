package com.hims.response;

import com.hims.entity.MasStoreItem;
import com.hims.entity.StoreInternalIndentM;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class IndentTResponse {

    private Long indentTId;


    private Long indentM;


    private Long itemId;


    private BigDecimal requestedQty;


    private BigDecimal approvedQty;

    private BigDecimal issuedQty;

    private BigDecimal receivedQty;


    private BigDecimal availableStock;


    private BigDecimal itemCost;


    private BigDecimal totalCost;


    private String issueStatus;
}



