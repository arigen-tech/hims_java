package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class MasRelationResponse {
    private Long id;
    private String relationName;
    private String code;
    private String status;
    private String lastChgBy;
    private Instant lastChgDate;
}
