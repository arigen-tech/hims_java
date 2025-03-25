package com.hims.response;

import lombok.Data;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;

@Data
public class DoctorRosterDTO {
    private Long Id;
    private Long hospitalId;
    private Long deptmentId;
    private Integer chgBy;
    private LocalDate chgDate;
    private String chgTime;
    private Long doctorId;
    private String rosterVal;
    private String  roasterDate;

}
