package com.hims.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class PendingForMandatoryTestingResponse {

    private Long donationId;
    private Long donorId;
    private String bagNumber;
    private String donorResNo;
    private String firstname;
    private String lastName;
    private String bloodGroup;
    private LocalDateTime collectionDateTime;
    private String collectionType;
    private Long noOfComponent;
    private String currentStatus;
    private String bagType;
    private LocalDateTime componentGenerationDateTime;


    public PendingForMandatoryTestingResponse(
            Long donationId,
            Long donorId,
            String bagNumber,
            String donorResNo,
            String firstname,
            String lastName,
            String bloodGroup,
            LocalDateTime collectionDateTime,
            String collectionType,
            Long noOfComponent,
            String currentStatus,
            String bagType,
            LocalDateTime componentGenerationDateTime
    ) {
        this.donationId = donationId;
        this.donorId = donorId;
        this.bagNumber = bagNumber;
        this.donorResNo = donorResNo;
        this.firstname = firstname;
        this.lastName = lastName;
        this.bloodGroup = bloodGroup;
        this.collectionDateTime = collectionDateTime;
        this.collectionType = collectionType;
        this.noOfComponent = noOfComponent;
        this.currentStatus = currentStatus;
        this.bagType = bagType;
        this.componentGenerationDateTime = componentGenerationDateTime;
    }
}