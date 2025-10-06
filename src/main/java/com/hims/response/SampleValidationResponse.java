package com.hims.response;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SampleValidationResponse {
    private Long patientId;
    private String patientName;
    private String sex;
    private String mobileNo;
    private String department;
    private String orderNo;
    private LocalDate orderDate;
    private LocalDateTime collectionTime;
    private String collectedBy;
    private String patientRelation;
    private List<TestDetailsDTO> investigations;


}
