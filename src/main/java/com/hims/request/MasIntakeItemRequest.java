package com.hims.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MasIntakeItemRequest {
    private Long intakeTypeId;
    private String intakeItemName;
}
