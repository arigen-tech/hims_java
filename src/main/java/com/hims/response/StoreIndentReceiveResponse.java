package com.hims.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StoreIndentReceiveResponse {

    private Long receiveMId;
    private String indentNo;
    private String issueNo;
    private LocalDateTime receivedDate;
    private String receivedBy;
    private String status;
    private String isReturn;
    private String message;
    private boolean returnCreated;
    private String returnMessage;
}
