package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ItemStockLedgerWithBatchResponse {
    private Long itemId;
    private String pvmsNo;
    private String nomenclature;
}
