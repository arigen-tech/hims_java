package com.hims.service.impl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.AppSetup;
import com.hims.entity.MasDepartment;
import com.hims.entity.MasOpdSession;
import com.hims.entity.User;
import com.hims.entity.repository.*;
import com.hims.exception.SDDException;
import com.hims.request.AppointmentReq;
import com.hims.request.AppointmentReqDaysKeys;
import com.hims.response.ApiResponse;
import com.hims.response.AppSetupDTO;
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
import java.time.ZoneId;
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
        AppsetupResponse res = new AppsetupResponse();
        try {
            MasDepartment department = departmentRepository.findById(appointmentReq.getDepartmentId())
                    .orElseThrow(() -> new SDDException("departmentId", 404, "Department not found"));
            User doctor = userRepo.findById(appointmentReq.getDoctorId())
                    .orElseThrow(() -> new SDDException("doctorId", 404, "Doctor not found"));
            MasOpdSession session = masOpdSessionRepository.findById(appointmentReq.getSessionId())
                    .orElseThrow(() -> new SDDException("sessionId", 404, "Session not found"));

            for (AppointmentReqDaysKeys key : appointmentReq.getDays()) {
                AppSetup entry;

                if (key.getId() != null) {
                    entry = appSetupRepository.findById(key.getId())
                            .orElseThrow(() -> new SDDException("id", 404, "Appointment setup not found"));
                } else {
                    // Check if an entry with the same departmentId, doctorId, and sessionId already exists
                    Optional<AppSetup> existingEntry = appSetupRepository.findByDeptAndDoctorIdAndSession(
                            department, doctor, session);

                    if (existingEntry.isPresent()) {
                        throw new SDDException("duplicate_entry", 409, "An appointment setup with these details already exists");
                    }

                    entry = new AppSetup();
                }

                entry.setDept(department);
                entry.setDoctorId(doctor);
                entry.setSession(session);

                entry.setStartTime(appointmentReq.getStartTime());
                entry.setEndTime(appointmentReq.getEndTime());
                entry.setTimeTaken(appointmentReq.getTimeTaken());

                entry.setDays(key.getDay());
                entry.setStartToken(key.getTokenStartNo());
                entry.setTotalInterval(key.getTokenInterval());
                entry.setTotalToken(key.getTotalToken());
                entry.setTotalOnlineToken(key.getTotalOnlineToken());
                entry.setMaxNoOfDays(key.getMaxNoOfDay());
                entry.setMinNoOfDays(key.getMinNoOfday());

                entry.setLastChgDate(Instant.now().atZone(ZoneId.systemDefault()).toLocalDate());
                entry.setLastChgBy(1);
                entry.setLastChgTime(Calender.getCurrentTimeStamp());

                appSetupRepository.save(entry);
            }

            res.setMsg("Success");
            return ResponseUtils.createSuccessResponse(res, new TypeReference<AppsetupResponse>() {});
        } catch (SDDException e) {
            return ResponseUtils.createFailureResponse(res, new TypeReference<AppsetupResponse>() {}, e.getMessage(), e.getStatus());
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(res, new TypeReference<AppsetupResponse>() {}, "Internal Server Error", 500);
        }
    }


//    @Override
//    public ApiResponse<AppsetupResponse> getappsetupData(Long deptId,Long doctorId,Long sessionId) {
//        AppsetupResponse res=new AppsetupResponse();
//        try {
//            List<AppSetup> appSetupList = appSetupRepository.findByDeptAndDoctorIdAndSessionId(departmentRepository.findById(deptId).get() , userRepo.findById(doctorId).get(), masOpdSessionRepository.findById(sessionId).get());
//            List<AppsetupgetResponse> appsetupgetResponses = new ArrayList<AppsetupgetResponse>();
//            for (Integer i = 0; i < appSetupList.size(); i++) {
//                AppsetupgetResponse appRes = new AppsetupgetResponse();
//                appRes.setDay(appSetupList.get(i).getDays());
//                appRes.setTokenStartNo(appSetupList.get(i).getStartToken());
//                appRes.setTotalToken(appSetupList.get(i).getTotalToken());
//                appRes.setTotalOnlineToken(appSetupList.get(i).getTotalOnlineToken());
//                appRes.setMaxNoOfDay(appSetupList.get(i).getMaxNoOfDays());
//                appRes.setMinNoOfday(appSetupList.get(i).getMinNoOfDays());
//                appsetupgetResponses.add(appRes);
//            }
//            res.setMsg("Success");
//            return ResponseUtils.createSuccessResponse(res, new TypeReference<AppsetupResponse>() {
//            });
//        } catch (Exception e) {
//            res.setMsg("Fail");
//            return ResponseUtils.createFailureResponse(res,new TypeReference<AppsetupResponse>() {
//            },e.getMessage(),500);
//        }
//    }



    @Override
    public ApiResponse<AppSetupDTO> getAppSetupDTO(Long deptId, Long doctorId, Long sessionId) {
        List<AppSetup> appSetups = appSetupRepository.findAppSetupsByIds(deptId, doctorId, sessionId);

        if (appSetups.isEmpty()) {
            return ResponseUtils.createSuccessResponse(null, new TypeReference<>() {});
        }

        AppSetupDTO wrapper = convertToWrapper(appSetups);
        return ResponseUtils.createSuccessResponse(wrapper, new TypeReference<>() {});
    }

    private AppSetupDTO convertToWrapper(List<AppSetup> appSetups) {
        AppSetupDTO wrapper = new AppSetupDTO();

        if (!appSetups.isEmpty()) {
            AppSetup firstAppSetup = appSetups.get(0);
            wrapper.setFromTime(firstAppSetup.getFromTime());
            wrapper.setToTime(firstAppSetup.getToTime());

            if (firstAppSetup.getHospital() != null) {
                wrapper.setHospitalId(firstAppSetup.getHospital().getId());
            }

            wrapper.setDeptId(firstAppSetup.getDept() != null ? firstAppSetup.getDept().getId() : null);
            wrapper.setValidFrom(firstAppSetup.getValidFrom());
            wrapper.setValidTo(firstAppSetup.getValidTo());
            wrapper.setDayOfWeek(firstAppSetup.getDayOfWeek());
            wrapper.setDoctorId(firstAppSetup.getDoctorId() != null ? firstAppSetup.getDoctorId().getUserId() : null);
            wrapper.setSessionId(firstAppSetup.getSession() != null ? firstAppSetup.getSession().getId() : null);
            wrapper.setStartTime(firstAppSetup.getStartTime());
            wrapper.setEndTime(firstAppSetup.getEndTime());
            wrapper.setTimeTaken(firstAppSetup.getTimeTaken());
        }

        // Create day-specific entries
        List<AppSetupDTO.appSetupDTO> daysList = new ArrayList<>();

        for (AppSetup appSetup : appSetups) {
            AppSetupDTO.appSetupDTO dayDTO = new AppSetupDTO.appSetupDTO();
            dayDTO.setId(appSetup.getId());
            dayDTO.setDays(appSetup.getDays());
            dayDTO.setMaxNoOfDays(appSetup.getMaxNoOfDays());
            dayDTO.setMinNoOfDays(appSetup.getMinNoOfDays());
            dayDTO.setTotalToken(appSetup.getTotalToken());
            dayDTO.setTotalInterval(appSetup.getTotalInterval());
            dayDTO.setStartToken(appSetup.getStartToken());
            dayDTO.setTotalOnlineToken(appSetup.getTotalOnlineToken());
            daysList.add(dayDTO);
        }

        wrapper.setDays(daysList);

        return wrapper;
    }

}
