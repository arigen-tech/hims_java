package com.hims.service;

import com.hims.entity.DoctorRoaster;
import com.hims.request.AppointmentReq;
import com.hims.request.DoctorRosterRequest;
import com.hims.response.ApiResponse;
import com.hims.response.AppsetupResponse;
import com.hims.response.DoctorRosterDTO;
import com.hims.response.DoctorRosterResponseDTO;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface DoctorRosterServices {
    ApiResponse<AppsetupResponse> doctorRoster(DoctorRosterRequest doctertReq);


    ApiResponse<List<DoctorRosterDTO>> getDoctorRoster(Long deptId, Long doctorId, LocalDate rosterDate);

    ApiResponse<DoctorRosterResponseDTO> getDoctorRostersWithDays(Long deptId, Long doctorId, LocalDate rosterDate, boolean isProduction);
}
