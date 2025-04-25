package com.hims.response;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
@Getter
@Setter
public class DgUomResponse {

    private Long id;
    private String uomCode;
    private String name;
    private String status;
    private String lastChgBy;
    private Instant lastChgDate;
    private String  lastChgTime;
}
