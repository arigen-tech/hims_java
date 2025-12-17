package com.hims.request;

import lombok.Data;

import java.util.List;

@Data
public class StoreInternalIssueRequest {
    private Long indentMId;
    private String issueType; // "partial" or "full"

//    private String remarks;
    private List<StoreInternalIssueDetailRequest> items;
}
