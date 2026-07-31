package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MasCommonStatusResponse {

    private Long commonStatusId;
    private String entityName;
    private String tableName;
    private String columnName;
    private String statusCode;
    private String statusName;
    private String statusDesc;
    private String remarks;
    private LocalDateTime updateDate;
}
