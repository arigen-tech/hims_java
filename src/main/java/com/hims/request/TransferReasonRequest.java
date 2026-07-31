package com.hims.request;

import lombok.Data;

@Data
public class TransferReasonRequest {

    private String reasonName;

    private String description;
}