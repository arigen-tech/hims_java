package com.hims.service;
import com.hims.entity.AppSetup;
import com.hims.entity.MasDepartment;
import com.hims.entity.MasOpdSession;
import com.hims.entity.User;
import com.hims.request.AppointmentReq;
import com.hims.response.ApiResponse;
import com.hims.response.AppSetupDTO;
import com.hims.response.AppsetupResponse;

import java.util.List;

public interface AppSetupServices {
    ApiResponse<AppsetupResponse> appSetup(AppointmentReq appointmentReq);

    ApiResponse<AppsetupResponse> getappsetupData(Long deptId,Long doctorId,Long sessionId);


    ApiResponse<AppSetupDTO> getAppSetupDTO(Long deptId, Long doctorId, Long sessionId);
}
