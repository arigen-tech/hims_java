package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MasTransferReasonResponse {

    private Long id;

    private String transferReasonName;

    private String code;

    private String status;

    private String lastChgBy;

    private LocalDateTime lastChgDate;
}