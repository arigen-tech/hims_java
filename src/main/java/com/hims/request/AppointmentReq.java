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
     private Long departmentId;
     private Long doctorId;
     private Long sessionId;
     private String startTime;
     private  String endTime;
     private  Integer timeTaken;
     private String opdLocation;
     private List<AppointmentReqDaysKeys> days;
    //MasDepartment departmentId;
//    AppointmentReqDays days;
//    AppointmentReqDaysKeys daysKeys;
}
