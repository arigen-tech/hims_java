package com.hims.service;

import com.hims.request.AppointmentReq;
import com.hims.request.DoctorRosterRequest;
import com.hims.response.*;

import java.time.LocalDate;
import java.util.List;

public interface AdminService {


    ApiResponse<AppsetupResponse> createOrUpdateAppointmentSetup(AppointmentReq request);

    ApiResponse<AppSetupDTO> getAppointmentSetup(Long deptId, Long doctorId, Long sessionId);

    ApiResponse<AppsetupResponse> createDoctorRoster(DoctorRosterRequest request);

    ApiResponse<List<DoctorRosterDTO>> getDoctorRoster(Long deptId, Long doctorId, LocalDate rosterDate, Long sessionId);

    ApiResponse<DoctorRosterResponseDTO> getDoctorRosterWeekly(Long deptId, Long doctorId, LocalDate parsedDate, boolean b);


}
