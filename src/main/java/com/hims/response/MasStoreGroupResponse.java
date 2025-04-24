package com.hims.response;

import lombok.Data;

import java.time.Instant;
@Data
public class MasStoreGroupResponse {

    private Integer id;
    private String groupCode;
    private String groupName;
    private String lastChgBy;
    private Instant lastChgDate;
    private String lastChgTime;
    private String status;


}
