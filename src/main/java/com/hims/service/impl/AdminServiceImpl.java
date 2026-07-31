package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.projection.AppSetupProjection;
import com.hims.projection.AvailableTokensProjection;
import com.hims.projection.DoctorRosterWeeklyProjection;
import com.hims.projection.GetDoctorRosterProjection;
import com.hims.request.DoctorRosterReqKeys;
import com.hims.request.DoctorRosterRequest;
import com.hims.response.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import com.hims.exception.SDDException;
import com.hims.request.AppointmentReq;
import com.hims.request.AppointmentReqDaysKeys;
import com.hims.service.AdminService;
import com.hims.utils.Calender;
import com.hims.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    private static final Logger log = LoggerFactory.getLogger(AppSetupServicesImpl.class);
    private static final ZoneId ZONE_ID = ZoneId.systemDefault();
    private static final DateTimeFormatter ROSTER_DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private AppSetupRepository appSetupRepository;

    @Autowired
    private MasDepartmentRepository departmentRepository;

    @Autowired
    private MasOpdSessionRepository masOpdSessionRepository;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private DoctorRoasterRepository doctorRoasterRepository;

    @Autowired
    private VisitRepository visitRepository;

    @Autowired
    private   MasServiceOpdRepository masServiceOpdRepository;

    @Value("${serviceCategoryOPD}")
    private String serviceCategoryOPD;

    @Autowired
    MasServiceCategoryRepository masServiceCategoryRepository;



    @Override
    public ApiResponse<AppsetupResponse> createOrUpdateAppointmentSetup(AppointmentReq appointmentReq) {
        AppsetupResponse res = new AppsetupResponse();
        try {
            log.info("appSetup called: deptId={}, doctorId={}, sessionId={}", appointmentReq.getDepartmentId(), appointmentReq.getDoctorId(),
                    appointmentReq.getSessionId());

            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }
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
                    long existingCount = appSetupRepository.countByDeptAndDoctorIdAndSession(department, doctor, session);

                    if (existingCount >= 7) {
                        throw new SDDException("duplicate_entry", 409, "An appointment setup with these details already exists 7 times");
                    }
                    entry = new AppSetup();
                    res.setMsg(AppConstants.APPOINTMENT_SETUP_SUCCESS_MSG);
                }
                entry.setDept(department);
                entry.setDoctorId(doctor);
                entry.setSession(session);
                entry.setStartTime(key.getStartTime());
                entry.setEndTime(key.getEndTime());
                entry.setTimeTaken(appointmentReq.getTimeTaken());
                entry.setDays(key.getDay());
                entry.setStartToken(key.getTokenStartNo());
                entry.setTotalInterval(key.getTokenInterval());
                entry.setTotalToken(key.getTotalToken());
                entry.setTotalOnlineToken(key.getTotalOnlineToken());
                entry.setMaxNoOfDays(key.getMaxNoOfDay());
                entry.setMinNoOfDays(key.getMinNoOfday());
                entry.setLastChgDate(Instant.now().atZone(ZoneId.systemDefault()).toLocalDate());
                entry.setLastChgBy(currentUser.getUserId().intValue());
                entry.setLastChgTime(Calender.getCurrentTimeStamp());
                entry.setHospital(currentUser.getHospital());
                entry.setOpdLocation(key.getOpdLocation());
                AppSetup saved = appSetupRepository.save(entry);
                log.info("AppSetup saved: id={}, day={}, startTime={}, endTime={}",
                        saved.getId(), key.getDay(), key.getStartTime(), key.getEndTime());
            }
            if(res.getMsg() == null) {
                res.setMsg(AppConstants.APPOINTMENT_UPDATE_SUCCESS_MSG);
            }
            log.info("appSetup completed successfully: deptId={}, doctorId={}, sessionId={}",
                    appointmentReq.getDepartmentId(), appointmentReq.getDoctorId(), appointmentReq.getSessionId());
            return ResponseUtils.createSuccessResponse(res, new TypeReference<AppsetupResponse>() {
            });
        } catch (SDDException e) {
            e.printStackTrace();
            return ResponseUtils.createFailureResponse(res, new TypeReference<AppsetupResponse>() {
            }, e.getMessage(), e.getStatus());
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(res, new TypeReference<AppsetupResponse>() {
            }, "Internal Server Error", 500);
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

    @Transactional(readOnly = true)
    @Override
    public ApiResponse<AppSetupDTO> getAppointmentSetup(Long deptId, Long doctorId, Long sessionId) {
        log.info("getAppSetUp called: deptId={}, doctorId={}, sessionId={}", deptId, doctorId, sessionId);
        try {

            List<AppSetupProjection> rows = appSetupRepository.findAppSetupById(deptId, doctorId, sessionId);
            if (rows.isEmpty()) {
                return ResponseUtils.createSuccessResponse(null, new TypeReference<>() {
                });
            }
            AppSetupDTO appSetupDTO = new AppSetupDTO();

            AppSetupProjection first = rows.get(0);

            appSetupDTO.setFromTime(first.getFromTime());
            appSetupDTO.setToTime(first.getToTime());
            appSetupDTO.setHospitalId(first.getHospitalId());
            appSetupDTO.setDeptId(first.getDeptId());
            appSetupDTO.setValidFrom(first.getValidFrom());
            appSetupDTO.setValidTo(first.getValidTo());
            appSetupDTO.setDayOfWeek(first.getDayOfWeek());
            appSetupDTO.setDoctorId(first.getDoctorId());
            appSetupDTO.setSessionId(first.getSessionId());
            appSetupDTO.setStartTime(first.getStartTime());
            appSetupDTO.setEndTime(first.getEndTime());
            appSetupDTO.setTimeTaken(first.getTimeTaken());
            // days list
            List<AppSetupDTO.appSetupDTO> daysList = new ArrayList<>(rows.size());
            for (AppSetupProjection r : rows) {
                AppSetupDTO.appSetupDTO d = new AppSetupDTO.appSetupDTO();
                d.setId(r.getId());
                d.setDays(r.getDays());
                d.setStartTime(r.getStartTime());
                d.setEndTime(r.getEndTime());
                d.setMaxNoOfDays(r.getMaxNoOfDays());
                d.setMinNoOfDays(r.getMinNoOfDays());
                d.setTotalToken(r.getTotalToken());
                d.setTotalInterval(r.getTotalInterval());
                d.setStartToken(r.getStartToken());
                d.setTotalOnlineToken(r.getTotalOnlineToken());
                d.setOpdLocation(r.getOpdLocation());
                daysList.add(d);
            }
            appSetupDTO.setDays(daysList);
            log.info("getAppSetUp success: daysCount={} (deptId={}, doctorId={}, sessionId={}",
                    daysList.size(), deptId, doctorId, sessionId);

            return ResponseUtils.createSuccessResponse(appSetupDTO, new TypeReference<>() {
            });

        } catch (Exception e) {
            log.error("Error in getAppSetUp: deptId={}, doctorId={}, sessionId={}", deptId, doctorId, sessionId, e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<AppSetupDTO>() {
            }, "Internal Server Error", 500);
        }
    }
    @Override
    public ApiResponse<AppsetupResponse> createDoctorRoster(DoctorRosterRequest doctorReq) {
        log.info("createDoctorRoster called: deptId={}, datesCount={}",
                doctorReq != null ? doctorReq.getDepartmentId() : null,
                (doctorReq != null && doctorReq.getDates() != null) ? doctorReq.getDates().size() : 0);
        AppsetupResponse res = new AppsetupResponse();
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }
            Optional<MasDepartment> deDepartment = departmentRepository.findById(doctorReq.getDepartmentId());
            if (deDepartment.isEmpty()) {
                throw new SDDException("departmentId", 404, "Department not found");
            }
            MasDepartment dept = deDepartment.get();
            Map<Long, User> doctorCache = new HashMap<>();
            for (DoctorRosterReqKeys key : doctorReq.getDates()) {
                Long doctorId = key.getDoctorId();
                User doctor = doctorCache.computeIfAbsent(doctorId, id ->
                        userRepo.findById(id)
                                .orElseThrow(() -> new SDDException("doctorId", 404, "Doctor not found"))
                );
                DoctorRoaster entry;
                if (key.getId() != null) {
                    entry = doctorRoasterRepository.findById(key.getId())
                            .orElseThrow(() -> new SDDException("rosterId", 404, "Roster entry not found"));
                } else {
                    entry = new DoctorRoaster();
                }
                entry.setRoasterDate(key.getDates());
                entry.setDoctorId(doctor);
                entry.setDepartment(dept);
                entry.setRoasterValue(key.getRosterVale());
                entry.setChgDate(Instant.now().atZone(ZoneId.systemDefault()).toLocalDate());
                entry.setChgBy(currentUser.getUserId());
                entry.setHospital(currentUser.getHospital());
                entry.setChgTime(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));

                doctorRoasterRepository.save(entry);
                log.info("Roster saved: rosterId={}, doctorId={}, deptId={}, rosterDate={}", entry.getId(), doctor, deDepartment.get().getId(), key.getDates());
            }

            res.setMsg("Success");
            log.info("createDoctorRoster success: deptId={}, savedCount={}",
                    doctorReq.getDepartmentId(), doctorReq.getDates().size());
            return ResponseUtils.createSuccessResponse(res, new TypeReference<AppsetupResponse>() {});
        } catch (Exception e) {
            e.printStackTrace();
            res.setMsg("Fail");
            return ResponseUtils.createFailureResponse(res, new TypeReference<AppsetupResponse>() {}, e.getMessage(), 500);
        }
    }


    @Transactional(readOnly = true)
    public ApiResponse<List<DoctorRosterDTO>> getDoctorRoster(Long deptId, Long doctorId, LocalDate rosterDate, Long sessionId) {

        log.info("getDoctorRoster called: deptId={}, doctorId={}, rosterDate={}, sessionId={}",
                deptId, doctorId, rosterDate, sessionId);

        Date convertedDate = java.sql.Date.valueOf(rosterDate);
        Instant currentDate = Instant.now();

        List<GetDoctorRosterProjection> rosterList;
        if (doctorId != null) {
            rosterList = doctorRoasterRepository.findDoctorRosterViewByDeptAndDoctor(deptId, doctorId, convertedDate);

            if (rosterList == null || rosterList.isEmpty()) {
                return ResponseUtils.createFailureResponse(new ArrayList<>(),
                        new TypeReference<List<DoctorRosterDTO>>() {},
                        "No doctor roster found",
                        HttpStatus.NOT_FOUND.value());
            }

            GetDoctorRosterProjection rosterObj = rosterList.get(0);

            MasServiceCategory category = masServiceCategoryRepository.findByServiceCateCode(serviceCategoryOPD);

            Optional<BigDecimal> baseTariffOpt = masServiceOpdRepository.findBaseTariffForDoctor(
                    rosterObj.getHospitalId(),
                    rosterObj.getDoctorId(),
                    rosterObj.getDeptmentId(),
                    category,
                    currentDate
            );

            if (baseTariffOpt.isEmpty() || baseTariffOpt.get() == null || baseTariffOpt.get().compareTo(BigDecimal.ZERO) == 0) {
                return ResponseUtils.createFailureResponse(
                        new ArrayList<>(),
                        new TypeReference<List<DoctorRosterDTO>>() {},
                        "Doctor Tariff is not Defined",
                        HttpStatus.NOT_FOUND.value()
                );
            }
        } else {
            rosterList = doctorRoasterRepository.findDoctorRosterViewByDept(deptId, convertedDate);
        }

        if (deptId != null && doctorId != null && rosterDate != null && sessionId != null) {
            // checkAppointmentAvailability needs DoctorRoaster entity -> ONLY 1 row fetch by id (minimal)
            GetDoctorRosterProjection first = rosterList.get(0);

            DoctorRoaster rosterEntity = doctorRoasterRepository.findById(first.getId())
                    .orElseThrow(() -> new SDDException("rosterId", 404, "Roster entry not found"));

            String availability = checkAppointmentAvailability(rosterEntity, deptId, doctorId, rosterDate, sessionId);

            if (!"SUCCESS".equals(availability)) {
                return ResponseUtils.createFailureResponse(
                        new ArrayList<>(),
                        new TypeReference<List<DoctorRosterDTO>>() {},
                        availability,
                        HttpStatus.BAD_REQUEST.value()
                );
            }
        }

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

        log.info("getDoctorRoster success: resultCount={} (deptId={}, doctorId={}, rosterDate={}, sessionId={})",
                dtoList.size(), deptId, doctorId, rosterDate, sessionId);

        return ResponseUtils.createSuccessResponse(dtoList, new TypeReference<List<DoctorRosterDTO>>() {});
    }


    @Override
    public ApiResponse<DoctorRosterResponseDTO> getDoctorRosterWeekly(
            Long deptId, Long doctorId, LocalDate rosterDate, boolean isProduction) {

        Date startDate = java.sql.Date.valueOf(rosterDate);
        Date endDate   = java.sql.Date.valueOf(rosterDate.plusDays(7));

        try {
            List<DoctorRosterWeeklyProjection> docRoster;
            if (doctorId != null) {
                docRoster = doctorRoasterRepository.findWeeklyByDeptAndDoctor(deptId, doctorId, startDate, endDate);
            } else {
                docRoster = doctorRoasterRepository.findWeeklyByDept(deptId, startDate, endDate);
            }
            if (docRoster == null || docRoster.isEmpty()) {
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
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            // filter (>= rosterDate), limit 7, map to DTO, date formatting
            List<DoctorRosterResponseDTO.DateEntry> dateEntries = docRoster.stream()
                    .filter(roster -> {
                        LocalDate rosterLocalDate = new java.sql.Date(roster.getRoasterDate().getTime()).toLocalDate();
                        return !rosterLocalDate.isBefore(rosterDate);
                    })
                    .limit(7)
                    .map(roster -> {
                        DoctorRosterResponseDTO.DateEntry entry = new DoctorRosterResponseDTO.DateEntry();
                        entry.setId(roster.getId());
                        entry.setDoctorId(roster.getDoctorUserId()); // SAME VALUE as before
                        entry.setRosterVale(roster.getRoasterValue());

                        LocalDate rosterLocalDate = new java.sql.Date(roster.getRoasterDate().getTime()).toLocalDate();
                        entry.setDates(rosterLocalDate.format(formatter));

                        return entry;
                    })
                    .collect(Collectors.toList());

            responseDTO.setDates(dateEntries);

            log.info("getDoctorRosterWeekly success: deptId={}, doctorId={}, from={}, resultSize={}",
                    deptId, doctorId, rosterDate, dateEntries.size());

            return ResponseUtils.createSuccessResponse(responseDTO, new TypeReference<DoctorRosterResponseDTO>() {});

        } catch (Exception e) {
            log.error("getDoctorRosterWeekly failed: deptId={}, doctorId={}, rosterDate={}",
                    deptId, doctorId, rosterDate, e);

            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<DoctorRosterResponseDTO>() {},
                    "Internal error while fetching doctor roster weekly",
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }


    private DoctorRosterDTO convertToDTO(GetDoctorRosterProjection v) {
        DoctorRosterDTO dto = new DoctorRosterDTO();

        dto.setDoctorId(v.getDoctorId());
        if (v.getRoasterDate() != null) {
            LocalDate roasterDate = v.getRoasterDate().toInstant()
                    .atZone(ZONE_ID)
                    .toLocalDate();
            dto.setRoasterDate(roasterDate.format(ROSTER_DATE_FMT));
        }
        dto.setRosterVal(v.getRosterVal());
        dto.setHospitalId(v.getHospitalId());
        dto.setId( v.getId().longValue());
        dto.setChgBy(v.getChgBy());
        dto.setChgDate(v.getChgDate());
        dto.setChgTime(v.getChgTime());
        dto.setDeptmentId(v.getDeptmentId());

        return dto;
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
            return "No more tokens available for today's session-1";
        }
        List<Long> existingTokens =
                visitRepository.findAllTokensForSessionToday(doctorId, hospitalId, sessionId);
        Long nextToken = getNextAvailableToken(existingTokens, startToken, maxToken);
        if (nextToken>=maxToken) {
            return "No more tokens available for today's session-1";
        }

        return "SUCCESS";
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

//    private AppSetupDTO convertToResponse(List<AppSetup> appSetups) {
//        AppSetupDTO wrapper = new AppSetupDTO();
//        if (!appSetups.isEmpty()) {
//            AppSetup firstAppSetup = appSetups.get(0);
//            wrapper.setFromTime(firstAppSetup.getFromTime());
//            wrapper.setToTime(firstAppSetup.getToTime());
//            if (firstAppSetup.getHospital() != null) {
//                wrapper.setHospitalId(firstAppSetup.getHospital().getId());
//            }
//            wrapper.setDeptId(firstAppSetup.getDept() != null ? firstAppSetup.getDept().getId() : null);
//            wrapper.setValidFrom(firstAppSetup.getValidFrom());
//            wrapper.setValidTo(firstAppSetup.getValidTo());
//            wrapper.setDayOfWeek(firstAppSetup.getDayOfWeek());
//            wrapper.setDoctorId(firstAppSetup.getDoctorId() != null ? firstAppSetup.getDoctorId().getUserId() : null);
//            wrapper.setSessionId(firstAppSetup.getSession() != null ? firstAppSetup.getSession().getId() : null);
//           wrapper.setStartTime(firstAppSetup.getStartTime());
//           wrapper.setEndTime(firstAppSetup.getEndTime());
//            wrapper.setTimeTaken(firstAppSetup.getTimeTaken());
//        }
//
//        // Create day-specific entries
//        List<AppSetupDTO.appSetupDTO> daysList = new ArrayList<>();
//        for (AppSetup appSetup : appSetups) {
//            AppSetupDTO.appSetupDTO dayDTO = new AppSetupDTO.appSetupDTO();
//            dayDTO.setStartTime(appSetup.getStartTime());
//            dayDTO.setEndTime(appSetup.getEndTime());
//            dayDTO.setId(appSetup.getId());
//            dayDTO.setDays(appSetup.getDays());
//            dayDTO.setMaxNoOfDays(appSetup.getMaxNoOfDays());
//            dayDTO.setMinNoOfDays(appSetup.getMinNoOfDays());
//            dayDTO.setTotalToken(appSetup.getTotalToken());
//            dayDTO.setTotalInterval(appSetup.getTotalInterval());
//            dayDTO.setStartToken(appSetup.getStartToken());
//            dayDTO.setTotalOnlineToken(appSetup.getTotalOnlineToken());
//            dayDTO.setOpdLocation(appSetup.getOpdLocation());
//            daysList.add(dayDTO);
//        }
//        wrapper.setDays(daysList);
//        return wrapper;
//    }
//

}


