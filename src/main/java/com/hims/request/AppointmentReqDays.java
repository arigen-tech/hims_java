package com.hims.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppointmentReqDays {
    AppointmentReqDaysKeys sunday;
    AppointmentReqDaysKeys monday;
    AppointmentReqDaysKeys tuesday;
    AppointmentReqDaysKeys wednesday;
    AppointmentReqDaysKeys thursday;
    AppointmentReqDaysKeys friday;
    AppointmentReqDaysKeys saturday;
}
