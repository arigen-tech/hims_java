package com.hims.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MasStoreSectionRequest {

    private String sectionCode;
    private String sectionName;
    private String status;
    private Integer masItemType;
}
