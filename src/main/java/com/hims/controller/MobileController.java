package com.hims.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.Patient;
import com.hims.entity.PatientLogin;
import com.hims.entity.repository.PatientLoginRepository;
import com.hims.entity.repository.PatientRepository;
import com.hims.jwt.JwtHelper;
import com.hims.request.LoginRequest;
import com.hims.request.OtpRequest;
import com.hims.response.*;
import com.hims.service.MasEmployeeService;
import com.hims.service.MobileLoginService;
import com.hims.utils.ResponseUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@Tag(name = "MobileController", description = "This controller is used for any ")
@RequestMapping("/mobileController")
@Slf4j
@RequiredArgsConstructor
public class MobileController {

    @Autowired
    MobileLoginService mobileLoginService;
    @Autowired
    private JwtHelper jwtUtil;
    @Autowired
    private MasEmployeeService masEmployeeService;
    @Autowired
    private PatientLoginRepository patientLoginRepository;
    @Autowired
    private PatientRepository patientRepository;



    @PostMapping("/mLogin")
    public ResponseEntity<ApiResponse> loginResponse(@RequestBody LoginRequest request) {
        return new ResponseEntity<>(mobileLoginService.loginRequest(request), HttpStatus.OK);
    }

    @PostMapping("/verifyOtp")
    public ApiResponse<?>  verifyOtp(@RequestBody @Valid OtpRequest otpRequest) {
        try {
            List<PatientLogin> patientLoginList = patientLoginRepository.findByMobileNoOrderByPatientLoginIdDesc(
                    otpRequest.getMobileNo()
            );
            String otp = otpRequest.getOtp();
            String sessionId = otpRequest.getSessionId();

            String uri = "https://2factor.in/API/V1/999a2629-21e1-11ec-a13b-0200cd936042/SMS/VERIFY/"
                    + sessionId + "/" + otp;

            HttpResponse<String> response = Unirest.post(uri)
                    .header("content-type", "application/x-www-form-urlencoded")
                    .asString();

            JSONObject json = new JSONObject(response.getBody());
            String status = json.getString("Status");

            //  Invalid OTP
            if (!"Success".equalsIgnoreCase(status)) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Invalid OTP", 400);
            }

            // OTP verified generate JWT
            String token = jwtUtil.mobileGenerateToken(
                    otpRequest.getMobileNo()
            );

            AuthResponse authResponse = new AuthResponse();
            List<PatientIdResponse> patientIdList = patientLoginList.stream()
                    .map(pl -> {
                        Patient patient = patientRepository.findById(pl.getPatientId()).orElse(null);
                        if (patient == null) return null;

                        PatientIdResponse pid = new PatientIdResponse();
                        pid.setPatientId(patient.getId());
                        String fullName = Stream.of(
                                        patient.getPatientFn(),
                                        patient.getPatientMn(),
                                        patient.getPatientLn()
                                ).filter(Objects::nonNull)
                                .filter(s -> !s.trim().isEmpty())
                                .collect(Collectors.joining(" "));

                        pid.setPatientName(fullName.isEmpty() ? null : fullName);
                        pid.setAge(patient.getPatientAge());
                        pid.setGender(patient.getPatientGender() != null ? patient.getPatientGender().getGenderName() : null);
                        pid.setPatientPhoneNumber(patient.getPatientMobileNumber());
                        pid.setRelation(patient.getPatientRelation() != null ? patient.getPatientRelation().getRelationName() : null);
                        return pid;
                    })
                    .toList();
            authResponse.setPatientIdResponseList(patientIdList);
            authResponse.setToken(token);
            authResponse.setMessage("OTP verified successfully");
            return ResponseUtils.createSuccessResponse( authResponse, new TypeReference<>() {});

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Unable to verify OTP Please try again.",
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }
    private ResponseEntity<ApiResponse> createErrorResponse(String message) {
        return ResponseEntity.badRequest().body(new ApiResponse("error", message, null));
    }

    @GetMapping("/searchBySpecialityAndDoctor")
    public ApiResponse<List<SpecialitiesAndDoctorResponse>> searchBySpecialityAndDoctor(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long hospitalId) {
        return masEmployeeService.getDepartmentAndDoctor(search, hospitalId);
    }
    @GetMapping("/getAllDoctorBySpecialityWise")
    public ApiResponse<List<SpecialityResponse>> getAllDoctorBySpecialityWise(@RequestParam(required = false) Long specialityId) {
        return masEmployeeService.getSpecialityAndDoctor(specialityId);
    }
    @GetMapping("/getDoctorDetailById")
    public ApiResponse<DoctorDetailResponse> getDoctorDetailById(@RequestParam(required = false) Long doctorId) {
       return masEmployeeService.getDoctor(doctorId);
    }
    /**
     * Fetches appointment history for a patient.
     *
     * @param hospitalId Hospital ID (required)
     * @param patientId Patient ID (optional if mobileNo provided)
     * @param mobileNo Mobile number (optional if patientId provided)
     * @param deptTypeCode Department type code (required)
     * @param includeAllHistory true (default) = all appointments (past + future), false = only future appointments
     * @return Appointment history based on flag
     */
    @GetMapping("/getAppointmentHistoryList")
    public ApiResponse<List<AppointmentBookingHistoryResponseDetails>> getAppointmentHistoryList(
            @RequestParam(required = true) Long hospitalId,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) String mobileNo,
            @RequestParam(required = false) String deptTypeCode,
            @RequestParam(required = false, defaultValue = "true") Boolean includeAllHistory
    ) {
        return masEmployeeService.appointmentHistoryList(hospitalId, patientId, mobileNo, deptTypeCode, includeAllHistory);
    }

}
