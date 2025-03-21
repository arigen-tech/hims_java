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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
    public List<DoctorRosterDTO> getDoctorRoster(Long deptId, Long doctorId, LocalDate rosterDate) {
        Date convertedDate = java.sql.Date.valueOf(rosterDate);

        List<DoctorRoaster> docRoster;

        if (doctorId != null) {
            docRoster = doctorRoasterRepository.findDoctorRosterByDeptAndDoctor(deptId, doctorId, convertedDate);
        } else {
            docRoster = doctorRoasterRepository.findDoctorRosterByDept(deptId, convertedDate);
        }

        return docRoster.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }



    private DoctorRosterDTO convertToDTO(DoctorRoaster roster) {
        DoctorRosterDTO dto = new DoctorRosterDTO();
        dto.setFromTime(roster.getChgTime());
        dto.setRosterVal(roster.getRoasterValue());
        dto.setToTime(roster.getChgTime());
        dto.setHospitalId(roster.getHospital() != null ? roster.getHospital().getId() : null);
        dto.setDeptId(roster.getDepartment() != null ? roster.getDepartment().getId() : null);
        dto.setDoctorId(roster.getDoctorId() != null ? roster.getDoctorId().getUserId() : null);

        if (roster.getChgDate() != null) {
            dto.setValidFrom(roster.getChgDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
            dto.setValidTo(roster.getChgDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
        }

        return dto;
    }

//    @Override
//    public List<DoctorRosterDTO> getDoctorRostersWithDays(Long deptId, Long doctorId, LocalDate rosterDate) {
//        Date startDate = convertToDate(rosterDate);
//        Date endDate = convertToDate(rosterDate.plusDays(6));
//
//        List<DoctorRoaster> docRoster;
//
//        if (doctorId != null) {
//            docRoster = doctorRoasterRepository.findDoctorRostersByDeptAndDoctor(deptId, doctorId, startDate, endDate);
//        } else {
//            docRoster = doctorRoasterRepository.findDoctorRostersByDept(deptId, startDate, endDate);
//        }
//
//        return docRoster.stream()
//                .map(this::convertToDTOs)
//                .collect(Collectors.toList());
//    }

    private Date convertToDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private DoctorRosterDTO convertToDTOs(DoctorRoaster roster) {
        DoctorRosterDTO dto = new DoctorRosterDTO();

        dto.setFromTime(roster.getChgTime());
        dto.setToTime(roster.getChgTime());
        dto.setRosterVal(roster.getRoasterValue());
        dto.setRoasterDate(roster.getRoasterDate());

        dto.setHospitalId(roster.getHospital() != null ? roster.getHospital().getId() : null);
        dto.setDeptId(roster.getDepartment() != null ? roster.getDepartment().getId() : null);
        dto.setDoctorId(roster.getDoctorId() != null ? roster.getDoctorId().getUserId() : null);

        if (roster.getChgDate() != null) {
            dto.setValidFrom(roster.getChgDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
            dto.setValidTo(roster.getChgDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
        }

        return dto;
    }


    @Override
    public DoctorRosterResponseDTO getDoctorRostersWithDays(Long deptId, Long doctorId, LocalDate rosterDate) {
        Date startDate = java.sql.Date.valueOf(rosterDate);
        Date endDate = java.sql.Date.valueOf(rosterDate.plusDays(7));

        List<DoctorRoaster> docRoster;
        if (doctorId != null) {
            docRoster = doctorRoasterRepository.findDoctorRostersByDeptAndDoctor(deptId, doctorId, startDate, endDate);
        } else {
            docRoster = doctorRoasterRepository.findDoctorRostersByDept(deptId, startDate, endDate);
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
                    entry.setDates(roster.getRoasterDate().toInstant());
                    return entry;
                })
                .collect(Collectors.toList());

        responseDTO.setDates(dateEntries);
        return responseDTO;
    }

}
