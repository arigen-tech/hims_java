package com.hims.response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;



@Data
public class MasUserTypeResponse {

    private Long userTypeId;
    private String userTypeName;
    private String status;
    private Long lastChgBy;
    private Instant lastChgDate;
    private String hospitalStaff;
    private Long mapId;
}
