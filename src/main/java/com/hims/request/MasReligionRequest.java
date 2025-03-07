package com.hims.request;

import lombok.Data;

import java.time.Instant;

@Data
public class MasReligionRequest {
    private String name;         // Name of the religion
    private String status;       // Status of the religion ("Y" for active, "N" for inactive)
    private String lastChgBy;   // User who last changed the record
    private Instant lastChgDate; // Timestamp of the last change
}