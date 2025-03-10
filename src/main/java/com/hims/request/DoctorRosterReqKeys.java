package com.hims.request;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class DoctorRosterReqKeys {
    Date dates;
    String rosterVale;
    Long doctorId;
    Long id;
}
