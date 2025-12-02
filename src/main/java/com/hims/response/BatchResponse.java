package com.hims.response;

import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDate;

@Data
public class BatchResponse {

    private String batchNo;
    private LocalDate manufactureDate;
    private LocalDate expiryDate;
    private  Long Storestocks;
    private  Long Dispstocks;
    private  Long wardstocks ;
    private  Long batchstock;
}
