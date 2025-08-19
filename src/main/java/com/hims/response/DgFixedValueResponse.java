package com.hims.response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class DgFixedValueResponse {
    private Long fixedId;
    private String fixedValue;
    private long subChargeCodeId;
}
