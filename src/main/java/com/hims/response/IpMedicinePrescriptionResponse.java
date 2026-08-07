package com.hims.response;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class IpMedicinePrescriptionResponse {
    private Long prescriptionId;
    private Long inpatientId;
    private Long itemId;
    private String itemName;
    private Long routeId;
    private String routeName;
    private String dose;
    private Long frequencyId;
    private String frequencyName;
    private LocalDateTime startDate;
    private LocalDateTime stopDate;
    private String administratedBy;
}