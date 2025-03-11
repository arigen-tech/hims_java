package com.hims.request;

import lombok.Data;

import java.time.Instant;

@Data
public class MasReligionRequest {
    private String name;
    private String status;
    private String lastChgBy;

}