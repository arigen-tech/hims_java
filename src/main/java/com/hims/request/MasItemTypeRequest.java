package com.hims.request;

import lombok.Data;

@Data
public class MasItemTypeRequest {
    private String code;
    private String name;
    private String status;
    private Integer masStoreGroupId;

}
