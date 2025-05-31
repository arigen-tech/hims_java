package com.hims.request;

import lombok.Data;

import java.time.Instant;

@Data
public class MasRelationRequest {
    private String relationName; // Name of the relation
    private String code;         // Code for the relation
}