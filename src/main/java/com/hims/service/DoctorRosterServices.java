package com.hims.service;

import com.hims.entity.DoctorRoaster;
import com.hims.request.AppointmentReq;
import com.hims.request.DoctorRosterRequest;
import com.hims.response.ApiResponse;
import com.hims.response.AppsetupResponse;
import com.hims.response.DoctorRosterDTO;

import java.util.Date;

public interface DoctorRosterServices {
    ApiResponse<AppsetupResponse> doctorRoster(DoctorRosterRequest doctertReq);


    DoctorRosterDTO getDoctorRoster(Long deptId, Long doctorId, java.time.LocalDate rosterDate);
}
