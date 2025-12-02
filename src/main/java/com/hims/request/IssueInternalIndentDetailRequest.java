package com.hims.request;

import lombok.Data;

import java.math.BigDecimal;


@Data

public class IssueInternalIndentDetailRequest {


        private Long indentTId;
        private Long itemId;
        private BigDecimal requestedQty;
        private BigDecimal approveQty;   // approve quantity used in the method
        private String reason;


}
