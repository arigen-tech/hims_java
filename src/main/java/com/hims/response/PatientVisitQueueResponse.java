package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PatientVisitQueueResponse {

    private Long visitId;
    private Long patientId;
    private LocalDateTime appointmentStartTime;
    private LocalDateTime appointmentEndTime;
    private String visitStatus;
    private String patientName;
    private String age;
    private String gender;
    private String mobileNumber;
}
