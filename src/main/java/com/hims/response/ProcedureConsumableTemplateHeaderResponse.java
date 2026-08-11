package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcedureConsumableTemplateHeaderResponse {
    private Long templateId;
    private String templateCode;
    private String templateName;

}