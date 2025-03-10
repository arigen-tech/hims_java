package com.hims.service.impl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.AppSetup;
import com.hims.entity.DoctorRoaster;
import com.hims.entity.MasDepartment;
import com.hims.entity.User;
import com.hims.entity.repository.*;
import com.hims.request.DoctorRosterReqKeys;
import com.hims.request.DoctorRosterRequest;
import com.hims.response.ApiResponse;
import com.hims.response.AppSetupDTO;
import com.hims.response.AppsetupResponse;
import com.hims.service.DoctorRosterServices;
import com.hims.utils.Calender;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DoctorRosterServicesImpl implements DoctorRosterServices {
@Autowired
DoctorRoasterRepository doctorRoasterRepository;
    @Autowired
    MasOpdSessionRepository masOpdSessionRepository;
    @Autowired
    UserRepo userRepo;
    @Autowired
    MasDepartmentRepository departmentRepository;
    @Override
    public ApiResponse<AppsetupResponse> doctorRoster(DoctorRosterRequest doctorReq) {
        AppsetupResponse res=new AppsetupResponse();
        try {
            Optional<MasDepartment> deDepartment = departmentRepository.findById(doctorReq.getDepartmentId());
           /// Optional<User> doctId = userRepo.findById(doctorReq.getDoctorId());
            Date date=doctorReq.getFromDate();

            for (DoctorRosterReqKeys key : doctorReq.getDates()) {
                DoctorRoaster entry = new DoctorRoaster();
                entry.setId(key.getId());
                entry.setRoasterDate(key.getDates());
                entry.setDoctorId(key.getDoctorId());
                entry.setDepartment(deDepartment.get());
                entry.setRoasterDate(date);
                entry.setRoasterValue(key.getRosterVale());
                entry.setChgDate(Instant.now().atZone(ZoneId.systemDefault()).toLocalDate());
                entry.setChgBy(1);
                //entry.setHospital(1);
                entry.setChgTime(Calender.getCurrentTimeStamp());
                doctorRoasterRepository.save(entry);
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
    // past code here,,, share on whatsapp...........
}
