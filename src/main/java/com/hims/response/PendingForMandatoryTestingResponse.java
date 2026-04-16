package com.hims.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PendingForMandatoryTestingResponse {

    private Long donationId;
    private Long donorId;
    private String bagNumber;
    private String donorResNo;
    private String fullName;
    private String bloodGroup;
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm", timezone = "Asia/Kolkata")
    private LocalDateTime collectionDateTime;
    private String collectionType;
    private Long noOfComponent;
    private String currentStatus;
    private String bagType;
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm", timezone = "Asia/Kolkata")
    private LocalDateTime componentGenerationDateTime;
}