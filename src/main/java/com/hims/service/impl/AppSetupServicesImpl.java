package com.hims.service.impl;

import com.hims.entity.AppSetup;
import com.hims.entity.MasDepartment;
import com.hims.entity.MasOpdSession;
import com.hims.entity.User;
import com.hims.entity.repository.AppSetupRepository;
import com.hims.entity.repository.MasDepartmentRepository;
import com.hims.request.AppointmentReq;

import com.hims.request.AppointmentReqDaysKeys;
import com.hims.response.ApiResponse;
import com.hims.response.AppsetupResponse;
import com.hims.service.AppSetupServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class AppSetupServicesImpl implements AppSetupServices {
    @Autowired
    AppSetupRepository appSetupRepository;
    @Autowired
    MasDepartmentRepository departmentRepository;

    @Override
    public ApiResponse<AppsetupResponse> appSetup(AppointmentReq appointmentReq) {



        Long departmentId=appointmentReq.getDepartmentId();
        //MasDepartment departmentId=appointmentReq.getDepartmentId();
        User doctorId=appointmentReq.getDoctorId();
        MasOpdSession sessionId=appointmentReq.getSessionId();
        String startTime=appointmentReq.getStartTime();
        String endTime=appointmentReq.getEndTime();
        Integer timeTaken=appointmentReq.getTimeTaken();
        Optional<MasDepartment> deDepartment = departmentRepository.findById(departmentId);

        for(AppointmentReqDaysKeys key:appointmentReq.getDays()) {
            AppSetup entry = new AppSetup();
            entry.setTimeTaken(appointmentReq.getTimeTaken());
            entry.setDays(key.getDay());
           // entry.setDept(appointmentReq.getDepartmentId());
            entry.setDept(deDepartment.get());

        }

       // List<AppointmentReqDaysKeys> days;
       // appointmentReq.getDays().getTuesday().getMinNoOfday();
        return null;
    }
}
