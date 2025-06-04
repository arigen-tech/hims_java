package com.hims.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MasItemClassRequest {
    private String itemClassCode;
    private String itemClassName;
    private String status;
    private Integer sectionId;


}
