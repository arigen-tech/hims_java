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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
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

    private static final Logger log = LoggerFactory.getLogger(DoctorRosterServicesImpl.class);

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
        AppsetupResponse res = new AppsetupResponse();
        try {
            Optional<MasDepartment> deDepartment = departmentRepository.findById(doctorReq.getDepartmentId());
            if (deDepartment.isEmpty()) {
                throw new SDDException("departmentId", 404, "Department not found");
            }

            for (DoctorRosterReqKeys key : doctorReq.getDates()) {
                User doctor = userRepo.findById(key.getDoctorId())
                        .orElseThrow(() -> new SDDException("doctorId", 404, "Doctor not found"));

                DoctorRoaster entry;

                if (key.getId() != null) {
                    entry = doctorRoasterRepository.findById(key.getId())
                            .orElseThrow(() -> new SDDException("rosterId", 404, "Roster entry not found"));
                } else {
                    entry = new DoctorRoaster();
                }

                User currentUser = getCurrentUser();
                if (currentUser == null) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                            "Current user not found", HttpStatus.UNAUTHORIZED.value());
                }

                entry.setRoasterDate(key.getDates());
                entry.setDoctorId(doctor);
                entry.setDepartment(deDepartment.get());
                entry.setRoasterValue(key.getRosterVale());
                entry.setChgDate(Instant.now().atZone(ZoneId.systemDefault()).toLocalDate());
                entry.setChgBy(currentUser.getUserId());
                entry.setHospital(currentUser.getHospital());
                entry.setChgTime(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));

                doctorRoasterRepository.save(entry);
            }

            res.setMsg("Success");
            return ResponseUtils.createSuccessResponse(res, new TypeReference<AppsetupResponse>() {});
        } catch (Exception e) {
            res.setMsg("Fail");
            return ResponseUtils.createFailureResponse(res, new TypeReference<AppsetupResponse>() {}, e.getMessage(), 500);
        }
    }


    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUserName(username);
        if (user == null) {
            log.warn("User not found for username: {}", username);
        }
        return user;
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

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy"); // Format for the response

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

                    // Format the roster date to "dd/MM/yyyy"
                    LocalDate rosterLocalDate = new java.sql.Date(roster.getRoasterDate().getTime()).toLocalDate();
                    String formattedDate = rosterLocalDate.format(formatter); // Apply the formatter here
                    entry.setDates(formattedDate);

                    return entry;
                })
                .collect(Collectors.toList());

        responseDTO.setDates(dateEntries);

        return ResponseUtils.createSuccessResponse(responseDTO, new TypeReference<DoctorRosterResponseDTO>() {});
    }

}
