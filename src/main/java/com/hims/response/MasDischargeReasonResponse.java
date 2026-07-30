package com.hims.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasDischargeReasonResponse {

    private Long id;
    private String reasonCode;
    private String reasonName;
}
