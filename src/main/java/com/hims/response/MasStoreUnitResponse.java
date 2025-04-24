package com.hims.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.*;

@Getter
@Setter
public class MasStoreUnitResponse {
    private long unitId;
    private String unitName;
    private String status;
    private String lastChgBy;


    private Instant lastChgDate;


    private String lastChgTime;
}
