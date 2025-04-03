package com.hims.request;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class DoctorRosterRequest {
    Long departmentId;
    Date fromDate;

    List<DoctorRosterReqKeys> dates;
}
