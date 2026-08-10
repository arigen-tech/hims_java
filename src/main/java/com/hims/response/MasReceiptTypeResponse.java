package com.hims.response;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MasReceiptTypeResponse {

    private Long receiptTypeId;
    private String receiptTypeCode;
    private String receiptTypeName;
}