package com.hims.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MasServiceOpdResponse {
     private Long id;
    private  String serviceName;
    private BigDecimal baseTariff;
    private  String serviceCategory;
    private  String departmentName;
    private String doctorFirstName;
    private String doctorMiddleName;
    private String doctorLastName;
    private  LocalDate fromDate;
    private  LocalDate toDate;
    private String  status;

}
