package com.hims.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VerifyReturnIndentDetailRequest {

    private Long returnTId;
    private Long stockId;
    private BigDecimal damagedQty;
    private String reason;
}
