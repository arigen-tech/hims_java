package com.hims.request;
import com.hims.entity.AppSetup;
import com.hims.entity.MasDepartment;
import com.hims.entity.MasOpdSession;
import com.hims.entity.User;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AppointmentReq {

    Long departmentId;
    //MasDepartment departmentId;
    Long doctorId;
    Long sessionId;
    String startTime;
    String endTime;
    Integer timeTaken;
    String opdLocation;
    List<AppointmentReqDaysKeys> days;
//    AppointmentReqDays days;
//    AppointmentReqDaysKeys daysKeys;
}
