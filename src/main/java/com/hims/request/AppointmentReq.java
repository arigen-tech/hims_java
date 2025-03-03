package com.hims.request;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppointmentReq {
    String departmentId;
    String doctorId;
    String sessionId;
    String startTime;
    String endTime;
    String timeTaken;
    AppointmentReqDays days;
//    AppointmentReqDaysKeys daysKeys;
}
