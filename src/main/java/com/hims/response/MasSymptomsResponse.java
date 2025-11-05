package com.hims.response;

import lombok.Data;

import java.time.Instant;

@Data
public class MasSymptomsResponse {
    private long id;
    private String symptomsCode;
    private String symptomsName;
    private String status;
    private String lastChgBy;
    private Instant lastChgDate;
    private String mostCommonUse;
}
