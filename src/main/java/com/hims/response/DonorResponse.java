package com.hims.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DonorResponse {
    private Long donorId;
    private Long screeningId;
    private String donorCode;
    private String name;
    private String gender;
    private String mobileNo;
    private String bloodGroup;
    private LocalDate registrationDate;
    private String screeningResult;

}
