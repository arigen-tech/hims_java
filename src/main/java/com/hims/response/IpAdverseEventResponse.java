package com.hims.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IpAdverseEventResponse {
    private Long adverseEventId;
    private Long inpatientId;
    private Long medicationId;
    private String medicationName;
    private String reaction;
    private String severity;
    private String actionTaken;
    private LocalDateTime reactionDatetime;
    private String medicationStopped;
    private String doctorInformed;
    private Long informedDoctorId;
    private String informedDoctorName;
    private String patientConditionAfter;
    private Long routeId;
    private String routeName;
    private String dose;
}