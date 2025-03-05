package com.hims.service.impl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.AppSetup;
import com.hims.entity.MasDepartment;
import com.hims.entity.MasOpdSession;
import com.hims.entity.User;
import com.hims.entity.repository.*;
import com.hims.request.AppointmentReq;
import com.hims.request.AppointmentReqDaysKeys;
import com.hims.response.ApiResponse;
import com.hims.response.AppsetupResponse;
import com.hims.response.AppsetupgetResponse;
import com.hims.service.AppSetupServices;
import com.hims.utils.Calender;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@Transactional
public class AppSetupServicesImpl implements AppSetupServices {
    @Autowired
    AppSetupRepository appSetupRepository;
    @Autowired
    MasDepartmentRepository departmentRepository;
    @Autowired
    MasOpdSessionRepository masOpdSessionRepository;
    @Autowired
    UserRepo userRepo;

    @Override
    public ApiResponse<AppsetupResponse> appSetup(AppointmentReq appointmentReq) {
        AppsetupResponse res=new AppsetupResponse();
 try {
    Optional<MasDepartment> deDepartment = departmentRepository.findById(appointmentReq.getDepartmentId());
    Optional<User> doctId = userRepo.findById(appointmentReq.getDoctorId());
    Optional<MasOpdSession> sessionVal = masOpdSessionRepository.findById(appointmentReq.getSessionId());
    for (AppointmentReqDaysKeys key : appointmentReq.getDays()) {
        AppSetup entry = new AppSetup();
        entry.setTimeTaken(appointmentReq.getTimeTaken());
        entry.setDays(key.getDay());
        entry.setDept(deDepartment.get());
        entry.setDoctorId(doctId.get());
        entry.setSession(sessionVal.get());

        entry.setStartTime(appointmentReq.getStartTime());
        entry.setEndTime(appointmentReq.getEndTime());
        entry.setTimeTaken(appointmentReq.getTimeTaken());
        entry.setId(appointmentReq.getId());
        ///
        entry.setStartToken(key.getTokenStartNo());
        entry.setTotalInterval(key.getTokenInterval());
        entry.setTotalToken(key.getTotalToken());
        entry.setTotalOnlineToken(key.getTotalOnlineToken());
        entry.setMaxNoOfDays(key.getMaxNoOfDay());
        entry.setMinNoOfDays(key.getMinNoOfday());
        entry.setLastChgDate(LocalDate.from(Instant.now()));
        entry.setLastChgBy(1);
        //entry.setHospital(1);
        entry.setLastChgTime(Calender.getCurrentTimeStamp());
            appSetupRepository.save(entry);
    }
    res.setMsg("Success");
    return ResponseUtils.createSuccessResponse(res, new TypeReference<AppsetupResponse>() {
    });
} catch (Exception e) {
    res.setMsg("Fail");
    return ResponseUtils.createFailureResponse(res,new TypeReference<AppsetupResponse>() {
    },e.getMessage(),500);
      }
    }

    @Override
    public ApiResponse<AppsetupResponse> getappsetupData(Long deptId,Long doctorId,Long sessionId) {
        AppsetupResponse res=new AppsetupResponse();
        try {
            List<AppSetup> appSetupList = appSetupRepository.findByDeptAndDoctorIdAndSessionId(departmentRepository.findById(deptId).get() , userRepo.findById(doctorId).get(), masOpdSessionRepository.findById(sessionId).get());
            List<AppsetupgetResponse> appsetupgetResponses = new ArrayList<AppsetupgetResponse>();
            for (Integer i = 0; i < appSetupList.size(); i++) {
                AppsetupgetResponse appRes = new AppsetupgetResponse();
                appRes.setDay(appSetupList.get(i).getDays());
                appRes.setTokenStartNo(appSetupList.get(i).getStartToken());
                appRes.setTotalToken(appSetupList.get(i).getTotalToken());
                appRes.setTotalOnlineToken(appSetupList.get(i).getTotalOnlineToken());
                appRes.setMaxNoOfDay(appSetupList.get(i).getMaxNoOfDays());
                appRes.setMinNoOfday(appSetupList.get(i).getMinNoOfDays());
                appsetupgetResponses.add(appRes);
            }
            res.setMsg("Success");
            return ResponseUtils.createSuccessResponse(res, new TypeReference<AppsetupResponse>() {
            });
        } catch (Exception e) {
            res.setMsg("Fail");
            return ResponseUtils.createFailureResponse(res,new TypeReference<AppsetupResponse>() {
            },e.getMessage(),500);
        }
    }
}
