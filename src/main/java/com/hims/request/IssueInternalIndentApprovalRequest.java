package com.hims.request;

import lombok.Data;

import java.util.List;


@Data

public class IssueInternalIndentApprovalRequest {

    private Long indentMId;
    private String action; // "approved" or "rejected"
    private String remarks;
//    private List<Long> deletedT;
    private List<IssueInternalIndentDetailRequest> items;
}
