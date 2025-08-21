package com.hims.request;

import com.hims.entity.MasDepartment;
import com.hims.entity.MasStoreItem;
import lombok.Data;

import java.util.List;

@Data
public class IndentRequest {
    private Long fromDeptId;
    private Long toDeptId;
    private String createdBy;
    private List<IndentTRequest> indentReq;

}
