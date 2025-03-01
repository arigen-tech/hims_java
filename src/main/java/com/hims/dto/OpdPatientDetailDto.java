package com.hims.dto;

import com.hims.entity.OpdPatientDetail;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.Instant;

/**
 * DTO for {@link OpdPatientDetail}
 */
public record OpdPatientDetailDto(@NotNull Long opdPatientDetailsId, @Size(max = 40) String height,
                                  @Size(max = 40) String idealWeight, @Size(max = 40) String weight,
                                  @Size(max = 40) String pulse, @Size(max = 48) String temperature, Instant opdDate,
                                  @Size(max = 12) String rr, String bmi, @Size(max = 120) String spo2, Double varation,
                                  @Size(max = 3) String bpSystolic, @Size(max = 3) String bpDiastolic, String icdDiag,
                                  String workingDiag, String followUpFlag, Long followUpDays, String pastMedicalHistory,
                                  String presentComplaints, String familyHistory, String treatmentAdvice,
                                  String sosFlag, @Size(max = 500) String recmmdMedAdvice,
                                  @Size(max = 1) String medicineFlag, @Size(max = 1) String labFlag,
                                  @Size(max = 1) String radioFlag, @Size(max = 1) String referralFlag,
                                  @Size(max = 1) String mlcFlag, @Size(max = 100) String policeStation,
                                  @Size(max = 100) String policeName, PatientDto patient, VisitDto visit,
                                  MasDepartmentDto department, MasHospitalDto hospital, UserDto doctor,
                                  Instant lastChgDate, @Size(max = 200) String lastChgBy) implements Serializable {
}
