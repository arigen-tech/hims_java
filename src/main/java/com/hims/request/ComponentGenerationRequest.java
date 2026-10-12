package com.hims.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ComponentGenerationRequest {
    Long componentId;
    Integer volumeMl;
    LocalDate expiryDate;
}
