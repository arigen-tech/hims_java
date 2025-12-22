package com.hims.request;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class StoreIndentReceiveRequest {
    private Long indentMId;
    private String issueNo;
    private LocalDateTime receivingDate;
    private String remarks;
    private List<StoreIndentReceiveItemRequest> items;
}