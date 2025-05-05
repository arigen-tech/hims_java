package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
public class MasReligionResponse {
    private Long id;
    private String name;
    private String status;
    private String lastChgBy;
    private LocalDateTime lastChgDate;
}
