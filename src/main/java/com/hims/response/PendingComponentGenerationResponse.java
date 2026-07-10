package com.hims.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PendingComponentGenerationResponse {
    Long donationId;
    String bagNumber;
    String donorCode;
    String firstName;
    String lastName;
    String bloodGroup;
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm", timezone = "Asia/Kolkata")
    LocalDateTime collectionDate;
    String collectionType;
    String bagType;
    Integer collectedVolumeMl;
   String currentStatus;

}
