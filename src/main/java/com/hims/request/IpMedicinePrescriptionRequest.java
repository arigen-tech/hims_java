package com.hims.request;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class IpMedicinePrescriptionRequest {

    private Long inpatientId;
    private Long itemId;
    private Long routeId;
    private String dose;
    private Long frequencyId;
    private LocalDateTime startDate;
    private String administratedBy;
    private Long day;
}