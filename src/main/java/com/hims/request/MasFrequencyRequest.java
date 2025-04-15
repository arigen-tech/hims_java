package com.hims.request;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
public class MasFrequencyRequest {

//    private  String frequencyCode;
    private String frequencyName;
    private String status;
    private String lastChgBy;
    private Long orderNo;
//    private String frequency;

    private Double feq;


}
