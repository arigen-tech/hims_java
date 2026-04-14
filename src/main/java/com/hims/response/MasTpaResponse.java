package com.hims.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MasTpaResponse {
    private Long tpaId;
    private String tpaName;
    private String tpaCode;
    private String contactPerson;
    private String contactNo;

    private LocalDateTime lastChgDate;
    private String status;
}
