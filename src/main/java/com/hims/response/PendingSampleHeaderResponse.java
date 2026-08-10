package com.hims.response;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PendingSampleHeaderResponse {
    private LocalDate reqDate;
    private String patientName;
    private String relation;
    private String age;
    private String gender;
    private String mobile;
    private String department;
    private String doctorName;
    private String priority;
    private Long orderHdId;
    private Long visitId;
    private Long inPatientId;
}
