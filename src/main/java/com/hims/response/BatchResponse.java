package com.hims.response;

import jakarta.persistence.Column;
import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;

@Data
public class BatchResponse {

    private String batchNo;
    private LocalDate manufactureDate;
    private LocalDate expiryDate;
    private  Long storeStocks;
    private  Long dispStocks;
    private  Long wardStocks ;
    private  Long batchStock;

    private BigDecimal batchIssuedQty;
    private BigDecimal batchReceivedQty;
    private BigDecimal batchRejectedQty;
    private String manufacturerName;
    private String brandName;



}
