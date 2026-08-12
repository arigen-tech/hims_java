package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StockUpdateResponse {

    private Long stockId;

    private Long qtyBefore;

    private Long qtyOut;

    private Long qtyAfter;
}