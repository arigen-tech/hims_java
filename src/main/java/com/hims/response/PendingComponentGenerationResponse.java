package com.hims.response;

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
    LocalDateTime collectionDate;
    String collectionType;
    String bagType;
    Integer collectedVolumeMl;
   String currentStatus;

}
