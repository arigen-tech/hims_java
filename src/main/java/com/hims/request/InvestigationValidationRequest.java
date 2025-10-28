package com.hims.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvestigationValidationRequest {
    private Long detailId;
    private Boolean accepted;
   // private Boolean rejected;
   // private String reason;
}
