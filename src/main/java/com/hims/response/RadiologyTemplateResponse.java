package com.hims.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RadiologyTemplateResponse {
    private Long pacsTemplateId;
    private String templateCode;
    private String templateName;
    private Long subChargecodeId;
    private String subChargeCodeName;
    private String templateText;

}
