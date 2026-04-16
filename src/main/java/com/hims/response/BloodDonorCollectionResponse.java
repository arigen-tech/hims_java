package com.hims.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Data
public class BloodDonorCollectionResponse {
    private Long donorId;
    private String donorCode;
    private String firstName;
    private String lastName;
    private Long bloodGroupId;
    private String bloodGroup;
    private LocalDate lastScreening;
    private String hb;
    private BigDecimal weight;


}
