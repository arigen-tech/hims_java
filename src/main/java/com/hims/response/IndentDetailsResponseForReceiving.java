package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IndentDetailsResponseForReceiving {

    private Long indentMId;
    private Long indentTId;
    private Long itemId;
    private String itemName;
    private String pvmsNo;
    private  Long unitAUid;
    private String unitAuName;
    private String batchNo;
    private LocalDate mfgDate;
    private LocalDate expDate;
    private BigDecimal qtyDemanded;
    private BigDecimal qtyIssued;
    private BigDecimal previousReceivedQty;
    private String manufacturerName;
    private String brandName;

}
