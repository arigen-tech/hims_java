package com.hims.request;

import lombok.Data;

import java.time.Instant;

@Data
public class MasRelationRequest {
    private String relationName; // Name of the relation
    private String code;         // Code for the relation
    private String status;       // Status of the relation ("Y" for active, "N" for inactive)
    private String lastChgBy;   // User who last changed the record
    private Instant lastChgDate; // Timestamp of the last change
}