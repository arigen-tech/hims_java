package com.hims.response;

import lombok.Data;
import java.time.Instant;

@Data
public class DoctorRosterDTO {
    private String fromTime;
    private String toTime;
    private Long hospitalId;
    private Long deptId;
    private Instant validFrom;
    private Instant validTo;
    private Long doctorId;
}
