package com.hims.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StoreIssueMResponse {
    private Long StoreIssueMId;
    private String issueNo;
    private LocalDateTime issueDate;
    private Long indentMId;

}
