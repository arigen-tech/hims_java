package com.hims.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MasApplicationRequest {
    private String name;
    private String parentId;
    private String url;
    private Long orderNo;
    private Long appSequenceNo;
    private String status;
}