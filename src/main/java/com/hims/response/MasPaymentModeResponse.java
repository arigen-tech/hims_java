package com.hims.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MasPaymentModeResponse {
    private Long paymentModeId;
    private String modeCode;
    private String modeName;

}
