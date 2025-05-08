package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
public class MasRelationResponse {
    private Long id;
    private String relationName;
    private String status;
    private String lastChgBy;
    private LocalDateTime lastChgDate;
    private String code;
}
