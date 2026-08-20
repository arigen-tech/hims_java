package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class MasAnaesthesiaInstructionResponse {

    private Long anaesthesiaInstructionId;
    private String instructionType;
    private String instruction;
    private String status;
    private String lastChgBy;
    private LocalDateTime lastChgDate;
}
