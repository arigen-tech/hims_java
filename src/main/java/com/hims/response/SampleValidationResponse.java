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
    private Long headerId;
    private Long patientId;
    private String patientName;
    private String sex;
    private String age;
    private String mobileNo;
    private Long subChargeCodeId;
    private String subChargeCodeName;
    private String orderNo;
    private LocalDate orderDate;
    private LocalDateTime collectionTime;
    private String collectedBy;
    private String patientRelation;
    private List<TestDetailsDTO> investigations;
    public SampleValidationResponse(Long sampleCollectionHeaderId, Long id, String fullName, @NotNull String s, @Size(max = 50) String patientAge, @Size(max = 20) String patientMobileNumber, Long aLong, @Size(max = 100) String s1, Long sampleCollectionHeaderId1, @Size(max = 50) String uhidNo, LocalDate localDate, LocalDateTime collectionTime, String collectionBy, @Size(max = 30) @NotNull String s2, List<TestDetailsDTO> investigations) {
    }
}
