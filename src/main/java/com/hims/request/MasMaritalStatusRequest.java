package com.hims.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MasMaritalStatusRequest {
    private String name;
    private String status;
    private String lastChgBy;
}