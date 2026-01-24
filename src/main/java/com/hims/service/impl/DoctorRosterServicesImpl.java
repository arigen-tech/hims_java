package com.hims.service.impl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
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

    @Autowired
    AppSetupRepository appSetupRepository ;

    @Autowired
    VisitRepository visitRepository;

    @Autowired
    MasServiceOpdRepository masServiceOpdRepository;

    @Value("${serviceCategoryOPD}")
    private String serviceCategoryOPD;

    @Autowired
    MasServiceCategoryRepository masServiceCategoryRepository;

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



    public ApiResponse<List<DoctorRosterDTO>> getDoctorRoster(
            Long deptId, Long doctorId, LocalDate rosterDate, Long sessionId) {

        Date convertedDate = java.sql.Date.valueOf(rosterDate);
        Instant currentDate = Instant.now();
        List<DoctorRoaster> rosterList;
        if (doctorId != null) {
            rosterList = doctorRoasterRepository.findDoctorRosterByDeptAndDoctor(deptId, doctorId, convertedDate);
            if (rosterList == null || rosterList.isEmpty()) {
                return ResponseUtils.createFailureResponse(
                        new ArrayList<>(),
                        new TypeReference<List<DoctorRosterDTO>>() {},
                        "No doctor roster found",
                        HttpStatus.NOT_FOUND.value()
                );
            }
            DoctorRoaster rosterObj = rosterList.get(0);
            MasServiceCategory category = masServiceCategoryRepository
                    .findByServiceCateCode(serviceCategoryOPD);
            Optional<MasServiceOpd> serviceOpd =
                    masServiceOpdRepository
                            .findByHospitalIdAndDoctorUserIdAndDepartmentIdAndServiceCatIdAndCurrentDate(
                                    rosterObj.getHospital(),
                                    rosterObj.getDoctorId(),
                                    rosterObj.getDepartment(),
                                    category,
                                    currentDate
                            );
            if (serviceOpd.isEmpty() ||
                    serviceOpd.get().getBaseTariff() == null ||
                    serviceOpd.get().getBaseTariff().compareTo(BigDecimal.ZERO) == 0) {

                return ResponseUtils.createFailureResponse(
                        new ArrayList<>(),
                        new TypeReference<List<DoctorRosterDTO>>() {},
                        "Doctor Tariff is not Defined",
                        HttpStatus.NOT_FOUND.value()
                );
            }
        } else {
            rosterList = doctorRoasterRepository.findDoctorRosterByDept(deptId, convertedDate);
        }
        if (deptId != null && doctorId != null && rosterDate != null && sessionId != null) {
            DoctorRoaster roster = rosterList.get(0);
            String availability = checkAppointmentAvailability(
                    roster,
                    deptId,
                    doctorId,
                    rosterDate,
                    sessionId
            );

            if (!"SUCCESS".equals(availability)) {
                return ResponseUtils.createFailureResponse(
                        new ArrayList<>(),
                        new TypeReference<List<DoctorRosterDTO>>() {},
                        availability,
                        HttpStatus.BAD_REQUEST.value()
                );
            }
        }

        // STEP 4: Convert to DTO
        if (rosterList == null || rosterList.isEmpty()) {
            return ResponseUtils.createFailureResponse(
                    new ArrayList<>(),
                    new TypeReference<List<DoctorRosterDTO>>() {},
                    "No doctor roster found for the given parameters",
                    HttpStatus.NOT_FOUND.value()
            );
        }

        List<DoctorRosterDTO> dtoList = rosterList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(
                dtoList,
                new TypeReference<List<DoctorRosterDTO>>() {}
        );
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

    private Long getNextAvailableToken(List<Long> existingTokens, int startToken, int maxToken) {
        int expected = startToken;
        for (Long token : existingTokens) {
            if (token > maxToken) break;
            if (token != expected) return (long) expected;
            expected++;
        }
        if (expected > maxToken) {
            throw new IllegalStateException("All tokens are already assigned.");
        }
        return (long) expected;
    }

    private String checkAppointmentAvailability(
            DoctorRoaster roster,
            Long deptId,
            Long doctorId,
            LocalDate rosterDate,
            Long sessionId) {

        Long hospitalId = roster.getHospital().getId();
        String dayName = LocalDate.now()
                .getDayOfWeek()
                .name()
                .substring(0, 1)
                .toUpperCase() + LocalDate.now()
                .getDayOfWeek()
                .name()
                .substring(1)
                .toLowerCase();
        List<AppSetup> optionalSetup =
                appSetupRepository.findByDoctorHospitalSessionAndDayName(
                        doctorId, roster.getDepartment().getId(), sessionId, dayName);
        if (optionalSetup.isEmpty()) {
            return "Appointment Setup is not configured for today's session";
        }
        AppSetup setup = optionalSetup.stream()
                .filter(s -> s.getSession().getId().equals(sessionId))
                .findFirst()
                .orElse(null);
        int startToken = (setup.getStartToken() != null) ? setup.getStartToken() : 1;
        int maxToken = (setup.getTotalToken() != null) ? setup.getTotalToken() : Integer.MAX_VALUE;
        if (maxToken == 0) {
            return "No tokens available for today's session";
        }
        List<Long> existingTokens =
                visitRepository.findAllTokensForSessionToday(doctorId, hospitalId, sessionId);
        Long nextToken = getNextAvailableToken(existingTokens, startToken, maxToken);
        if (nextToken>=maxToken) {
            return "No tokens available for today's session";
        }

        return "SUCCESS";
    }

    @Override
//    public ApiResponse<List<AvailableTokenSlotResponse>> getAvailableToken(Long deptId, Long doctorId, String appointmentDate, Long sessionId) {
//        LocalDate date = LocalDate.parse(appointmentDate);
//        String dayName =
//                date.getDayOfWeek()
//                        .getDisplayName(TextStyle.FULL, Locale.ENGLISH);
//
//
//        List<AppSetup> optionalSetup = appSetupRepository.findByDoctorHospitalSessionAndDayName(
//                doctorId, deptId, sessionId, dayName);
//
//        AppSetup appSetup = optionalSetup.get(0);
//        int startToken = appSetup.getStartToken();
//        int intervalToken = appSetup.getTotalInterval()-1;
//        int totalToken = appSetup.getTotalToken();
//        int totalOnlineTokens = appSetup.getTotalOnlineToken();
//        int timeTakenMin = appSetup.getTimeTaken();
//        String startTime = appSetup.getStartTime();
//        String endTime = appSetup.getEndTime();
//
//        List<AvailableTokenSlotResponse> list = generateOfflineSlots(startToken,intervalToken,totalToken,totalOnlineTokens,startTime,endTime,timeTakenMin,deptId,doctorId,sessionId,date,visitRepository);
//        return ResponseUtils.createSuccessResponse(list, new TypeReference<List<AvailableTokenSlotResponse>>() {});
//    }


    public ApiResponse<List<AvailableTokenSlotResponse>> getAvailableToken(
            Long deptId, Long doctorId, String appointmentDate, Long sessionId,int flag) {

        LocalDate date = LocalDate.parse(appointmentDate);
        String dayName = date.getDayOfWeek()
                .getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        List<AppSetup> optionalSetup = appSetupRepository.findByDoctorHospitalSessionAndDayName(
                doctorId, deptId, sessionId, dayName);

        AppSetup appSetup = optionalSetup.get(0);
        int startToken = appSetup.getStartToken();
        int intervalToken = appSetup.getTotalInterval();
        int totalToken = appSetup.getTotalToken();
        int totalOnlineTokens = appSetup.getTotalOnlineToken();
        int timeTakenMin = appSetup.getTimeTaken();
        String startTime = appSetup.getStartTime();
        String endTime = appSetup.getEndTime();

        Instant startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
        Set<Long> occupiedTokens = new HashSet<>();
        try {
            occupiedTokens = visitRepository.findOccupiedTokens(
                            deptId, doctorId, sessionId, startOfDay, endOfDay)
                    .stream().collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("Error fetching occupied tokens", e);
        }

        List<AvailableTokenSlotResponse> list = generateSlotsWithAvailability(
                startToken, intervalToken, totalToken,
                startTime, endTime, timeTakenMin, occupiedTokens,flag);

        return ResponseUtils.createSuccessResponse(list, new TypeReference<List<AvailableTokenSlotResponse>>() {});
    }
//    public static List<AvailableTokenSlotResponse> generateSlotsWithAvailability(
//            int tokenStart,
//            int tokenInterval,
//            int totalTokens,
//            String dayStartTime,
//            String dayEndTime,
//            int timeTakenMin,
//            Set<Long> occupiedTokenNumbers,
//            int flag
//    ) {
//        List<AvailableTokenSlotResponse> slots = new ArrayList<>();
//        if (totalTokens <= 0 || timeTakenMin <= 0) {
//            return slots;
//        }
//        LocalTime start = LocalTime.parse(dayStartTime);
//        LocalTime end = LocalTime.parse(dayEndTime);
//        int slotIndex = 0;
//        for (int tokenNum = tokenStart; tokenNum <= totalTokens; tokenNum++) {
//            LocalTime slotStart = start.plusMinutes(slotIndex * timeTakenMin);
//            LocalTime slotEnd = slotStart.plusMinutes(timeTakenMin);
//            if (!slotStart.isBefore(end) || slotEnd.isAfter(end)) {
//                break;
//            }
//            boolean isOnline = tokenInterval > 0 && tokenNum % tokenInterval == 0;
//            boolean isAvailable = !occupiedTokenNumbers.contains((long) tokenNum);
//            boolean shouldAdd =
//                    tokenInterval == 0 ||
//                            (flag == 0 && !isOnline) ||
//                            (flag == 1 && isOnline);
//            if (shouldAdd) {
//                slots.add(new AvailableTokenSlotResponse(tokenNum, slotStart, slotEnd, isAvailable));
//            }
//            slotIndex++;
//        }
//        return slots;
//    }

//    // If you want to show booked tokens with isAvailable = false, use this version:
public static List<AvailableTokenSlotResponse> generateSlotsWithAvailability(
        int tokenStart,
        int tokenInterval, int totalTokens, String dayStartTime, String dayEndTime, int timeTakenMin, Set<Long> occupiedTokenNumbers, int flag) {

    List<AvailableTokenSlotResponse> slots = new ArrayList<>();

    if (totalTokens <= 0 || timeTakenMin <= 0) {
        return slots;
    }

    LocalTime start = LocalTime.parse(dayStartTime);
    LocalTime end = LocalTime.parse(dayEndTime);

    int slotIndex = 0;

    for (int tokenNum = tokenStart; tokenNum <= totalTokens; tokenNum++) {

        LocalTime slotStart = start.plusMinutes(slotIndex * timeTakenMin);
        LocalTime slotEnd = slotStart.plusMinutes(timeTakenMin);

        if (!slotStart.isBefore(end) || slotEnd.isAfter(end)) {
            break;
        }

        boolean isOnline = tokenInterval > 0 && tokenNum % tokenInterval == 0;
        boolean isAvailable = !occupiedTokenNumbers.contains((long) tokenNum);

        boolean shouldAdd =
                tokenInterval == 0 || (flag == 0 && !isOnline) || (flag == 1 && isOnline);

        if (shouldAdd) {
            slots.add(new AvailableTokenSlotResponse(
                    tokenNum,
                    slotStart,
                    slotEnd,
                    isAvailable
            ));
        }
        slotIndex++;
    }
    return slots;
}


}
