package com.hims.service.impl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.DoctorRoaster;
import com.hims.entity.MasDepartment;
import com.hims.entity.User;
import com.hims.entity.repository.*;
import com.hims.exception.SDDException;
import com.hims.request.DoctorRosterReqKeys;
import com.hims.request.DoctorRosterRequest;
import com.hims.response.*;
import com.hims.service.DoctorRosterServices;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
                User doctor = userRepo.findById(key.getDoctorId())
                        .orElseThrow(() -> new SDDException("doctorId", 404, "Doctor not found"));
                DoctorRoaster entry = new DoctorRoaster();
                entry.setId(key.getId());
                entry.setRoasterDate(key.getDates());
                entry.setDoctorId(doctor);
                entry.setDepartment(deDepartment.get());
                entry.setRoasterDate(key.getDates());
                entry.setRoasterValue(key.getRosterVale());
                entry.setChgDate(Instant.now().atZone(ZoneId.systemDefault()).toLocalDate());
                entry.setChgBy(1);
                //entry.setHospital(1);
                entry.setChgTime(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
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

    @Override
    public ApiResponse<List<DoctorRosterDTO>> getDoctorRoster(Long deptId, Long doctorId, LocalDate rosterDate) {
        Date convertedDate = java.sql.Date.valueOf(rosterDate);

        ApiResponse<List<DoctorRosterDTO>> apiResponse;

        List<DoctorRoaster> docRosterList;

        if (doctorId != null) {
            docRosterList = doctorRoasterRepository.findDoctorRosterByDeptAndDoctor(deptId, doctorId, convertedDate);
        } else {
            docRosterList = doctorRoasterRepository.findDoctorRosterByDept(deptId, convertedDate);
        }

        if (docRosterList != null && !docRosterList.isEmpty()) {
            List<DoctorRosterDTO> doctorRosterDTOList = docRosterList.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            apiResponse = ResponseUtils.createSuccessResponse(doctorRosterDTOList, new TypeReference<List<DoctorRosterDTO>>() {});
        } else {
            apiResponse = ResponseUtils.createFailureResponse(
                    new ArrayList<>(),
                    new TypeReference<List<DoctorRosterDTO>>() {},
                    "No doctor roster found for the given parameters",
                    HttpStatus.NOT_FOUND.value());
        }

        return apiResponse;
    }

    private DoctorRosterDTO convertToDTO(DoctorRoaster doctorRoaster) {


        DoctorRosterDTO dto = new DoctorRosterDTO();
        dto.setDoctorId(doctorRoaster.getDoctorId().getUserId());
        LocalDate roasterDate = doctorRoaster.getRoasterDate().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedRoasterDate = roasterDate.format(formatter);
        dto.setRoasterDate(formattedRoasterDate);
        dto.setRosterVal(doctorRoaster.getRoasterValue());
        dto.setHospitalId(doctorRoaster.getHospital().getId());
        dto.setId(doctorRoaster.getId());
        dto.setChgBy(doctorRoaster.getChgBy());
        dto.setChgDate(doctorRoaster.getChgDate());
        dto.setChgTime(doctorRoaster.getChgTime());
        dto.setDeptmentId(doctorRoaster.getDepartment().getId());
        return dto;
    }



    @Override
    public ApiResponse<DoctorRosterResponseDTO> getDoctorRostersWithDays(Long deptId, Long doctorId, LocalDate rosterDate, boolean isProduction) {
        Date startDate = java.sql.Date.valueOf(rosterDate);
        Date endDate = java.sql.Date.valueOf(rosterDate.plusDays(7));

        List<DoctorRoaster> docRoster;
        if (doctorId != null) {
            docRoster = doctorRoasterRepository.findDoctorRostersByDeptAndDoctor(deptId, doctorId, startDate, endDate);
        } else {
            docRoster = doctorRoasterRepository.findDoctorRostersByDept(deptId, startDate, endDate);
        }

        if (docRoster.isEmpty()) {
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<DoctorRosterResponseDTO>() {},
                    "No doctor rosters found for the given parameters",
                    HttpStatus.NOT_FOUND.value()
            );
        }

        DoctorRosterResponseDTO responseDTO = new DoctorRosterResponseDTO();
        responseDTO.setDepartmentId(deptId);
        responseDTO.setFromDate(rosterDate);

        List<DoctorRosterResponseDTO.DateEntry> dateEntries = docRoster.stream()
                .filter(roster -> {
                    LocalDate rosterLocalDate = new java.sql.Date(roster.getRoasterDate().getTime()).toLocalDate();
                    return !rosterLocalDate.isBefore(rosterDate);
                })
                .limit(7)
                .map(roster -> {
                    DoctorRosterResponseDTO.DateEntry entry = new DoctorRosterResponseDTO.DateEntry();
                    entry.setId(roster.getId());
                    entry.setDoctorId(roster.getDoctorId() != null ? roster.getDoctorId().getUserId() : null);
                    entry.setRosterVale(roster.getRoasterValue());
                    entry.setDates(new java.sql.Date(roster.getRoasterDate().getTime()).toLocalDate());
                    return entry;
                })
                .collect(Collectors.toList());

        responseDTO.setDates(dateEntries);

        return ResponseUtils.createSuccessResponse(responseDTO, new TypeReference<DoctorRosterResponseDTO>() {});
    }
}
