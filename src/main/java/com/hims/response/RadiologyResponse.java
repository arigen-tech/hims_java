package com.hims.response;

import com.hims.request.UserDepartmentRequestOne;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class RadiologyResponse {
    private String accessionNo;
    private String uhidNo;
    private String patientName;
    private String age;
    private String gender;
    private String modality;
    private String investigationName;
    private LocalDate orderDate;
    private Instant orderTime;
    private String Department;
}
