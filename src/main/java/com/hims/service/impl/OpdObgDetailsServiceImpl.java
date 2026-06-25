package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.OpdObgDetails;
import com.hims.entity.Patient;
import com.hims.entity.User;
import com.hims.entity.Visit;
import com.hims.entity.repository.OpdObgDetailsRepository;
import com.hims.entity.repository.PatientRepository;
import com.hims.entity.repository.VisitRepository;
import com.hims.projection.OpdObgDetailsProjection;
import com.hims.request.OpdObgDetailsRequest;
import com.hims.response.ApiResponse;
import com.hims.response.OpdObgDetailsResponse;
import com.hims.service.OpdObgDetailsService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpdObgDetailsServiceImpl implements OpdObgDetailsService {
    private final OpdObgDetailsRepository opdObgDetailsRepository;
    private final PatientRepository patientRepository;
    private final VisitRepository visitRepository;
    @Autowired
    private AuthUtil authUtil;

    @Override
    public ApiResponse<String> createOrUpdateObgDetails(Long visitId, OpdObgDetailsRequest request) {

            log.info("Creating or updating OBG details for visit ID: {}", visitId);
            // Validate visitId
            if (visitId == null || visitId <= 0) {
                log.warn("Invalid visit ID provided: {}", visitId);
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Invalid visit ID", HttpStatus.BAD_REQUEST.value());
            }
            User user = authUtil.getCurrentUser();
            Patient patient = patientRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new RuntimeException("Patient not found"));
            Visit visit = visitRepository.findById(visitId)
                    .orElseThrow(() -> new RuntimeException("Visit not found"));

            // Check if OBG details already exist for this visit
            OpdObgDetails entity = opdObgDetailsRepository.findByVisitId(visitId)
                    .orElse(new OpdObgDetails());

            boolean isNew = entity.getObgId() == null;
            String operation = isNew ? "Created" : "Updated";

            // Basic info
            entity.setPatient(patient);
            entity.setVisit(visit);
            entity.setOpdDate(request.getOpdDate());

            // GynaecologyHistory section
            if (request.getGynaecologyHistory() != null) {
                var gh = request.getGynaecologyHistory();
                entity.setGynFlow(gh.getGynFlow());
                entity.setGynMenarcheAge(gh.getAgeOfMenarche() != null ? String.valueOf(gh.getAgeOfMenarche()) : null);
                entity.setGynLastMenstrualPeriod(gh.getLastMenstrualPeriod());
                entity.setGynMenstrualPattern(gh.getMenstrualPattern());
                entity.setGynCycleType(gh.getGynCycle());
                entity.setSterilisation(gh.getSterilisation());
                entity.setGynObstetricHistory(gh.getGynObstetricHistory());

                // Examination from GynaecologyHistory
                entity.setAbdomenInspection(gh.getPerAbdomenInspection());
                entity.setAbdomenPalpation(gh.getAbdomenPalpation());
                entity.setPapSmearResult(gh.getPapSmear());
                entity.setLocalExaminationNotes(gh.getLocalExamination());
                entity.setPerSpeculum(gh.getPerSpeculum());
                entity.setBimanualExamination(gh.getBimanualExamination());
            }

            // OBGDetails section
            if (request.getObgDetails() != null) {
                var obg = request.getObgDetails();

                // Obstetric History
                entity.setObstetricHistoryNotes(obg.getObstetricHistory());

                // Obstetric Score
                if (obg.getObstetricScore() != null) {
                    var score = obg.getObstetricScore();
                    entity.setGravida(score.getGravida());
                    entity.setPara(score.getPara());
                    entity.setAbortions(score.getAbortion());
                    entity.setLivingChildren(score.getLivingChildren());
                }

                // Basic OBG fields
                entity.setConceptionType(obg.getConception());
                entity.setMarriedLifeYears(obg.getMarriedLife());
                entity.setConsanguinity(obg.getConsanguinity());
                entity.setBookedStatus(obg.getBooked());
                entity.setImmunisedStatus(obg.getImmunised());
                entity.setTrimester(obg.getTrimester());
                entity.setGc(obg.getGestationalCalculation());
                entity.setPedalEdema(obg.getPerExamination());
                entity.setTtStatus(obg.getTetanusToxoid());
                entity.setFhr(obg.getFetalHeartRate());
                entity.setPresentation(obg.getPresentation());
                entity.setPallor(obg.getPaPalpation());
                entity.setPalpationNotes(obg.getPalpation());

                // PV field
                if (obg.getPv() != null) {
                    entity.setPvDone(obg.getPv());
                }

                // Uterus height
                entity.setUterusHeight(obg.getInspectionHeightOfUterus());
                entity.setUterusHeightSpecify(obg.getSpecify());

                entity.setAntenatalRemarks(obg.getRemarks());

                // MenstrualHistory from OBGDetails
                if (obg.getMenstrualHistory() != null) {
                    var mh = obg.getMenstrualHistory();
                    entity.setMenarcheAge(mh.getAgeOfMenarche() != null ? String.valueOf(mh.getAgeOfMenarche()) : null);
                    entity.setCycles(mh.getCycles());
                    entity.setRangeDays(mh.getRangeDays() != null ? String.valueOf(mh.getRangeDays()) : null);
                    entity.setIntervalDays(mh.getInterval());
                    entity.setMenstrualFlow(mh.getFlow());
                    entity.setMenstrualPause(mh.getMenstrualPause());
                }

                // SystemicExamination from OBGDetails
                if (obg.getSystemicExamination() != null) {
                    var se = obg.getSystemicExamination();
                    entity.setRespiratorySystem(se.getRespiratorySystem());
                    entity.setBreathSounds(se.getBreathSounds());
                }

                // CardiovascularSystem from OBGDetails
                if (obg.getCardiovascularSystem() != null) {
                    var cv = obg.getCardiovascularSystem();
                    entity.setCardiovascularS1(cv.getS1());
                    entity.setCardiovascularS2(cv.getS2());
                    entity.setCardiovascularMurmurs(cv.getMurmurs());
                }

                // PerVaginalExamination from OBGDetails
                if (obg.getPerVaginalExamination() != null) {
                    var pv = obg.getPerVaginalExamination();
                    entity.setPvOsDilatation(pv.getOsDilatation());
                    entity.setPvEffacement(pv.getEffacement());
                    entity.setPvMembrane(pv.getMembrane());
                    entity.setPvLiquor(pv.getLiquor());
                    entity.setCervixConsistency(pv.getConsistency());
                    entity.setCervixPosition(pv.getPosition());
                    entity.setCervixLength(pv.getLength());
                    entity.setStationPresenting(pv.getStationOfPresentingPart());
                    entity.setFetalHead(pv.getHead());
                    entity.setPelvis(pv.getPelvis());
                }
            }

            // System fields
            entity.setStatus(AppConstants.STATUS_Y.toLowerCase());
            if (isNew) {
                entity.setCreatedBy(user.getFullName());
            }
            entity.setLastUpdatedBy(user.getFullName());
            entity.setLastUpdateDate(LocalDateTime.now());

            opdObgDetailsRepository.save(entity);

            log.info("Successfully {} OBG details for visit ID: {}", operation, visitId);
            return ResponseUtils.createSuccessResponse(
                    "OPD OBG Details " + operation + " successfully",
                    new TypeReference<>() {});
    }

    @Override
    public ApiResponse<OpdObgDetailsResponse> getObgDetailsByVisitId(Long visitId) {
        try {
            log.info("Fetching OBG details for visit ID: {}", visitId);
            if (visitId == null || visitId <= 0) {
                log.warn("Invalid visit ID provided: {}", visitId);
                return ResponseUtils.createFailureResponse(null, new TypeReference<OpdObgDetailsResponse>() {
                        },
                        "Invalid visit ID", HttpStatus.BAD_REQUEST.value());
            }
            if (!visitRepository.existsById(visitId)) {
                log.warn("Visit not found with ID: {}", visitId);
                return ResponseUtils.createFailureResponse(null, new TypeReference<OpdObgDetailsResponse>() {
                        },
                        "Visit not found", HttpStatus.NOT_FOUND.value());
            }
            var obgProjection = opdObgDetailsRepository.findOpdObgDetailsByVisitId(visitId);
            if (obgProjection.isPresent()) {
                log.info("Successfully retrieved OBG details for visit ID: {}", visitId);
                OpdObgDetailsResponse response = mapProjectionToResponse(obgProjection.get());
                return ResponseUtils.createSuccessResponse(response, new TypeReference<OpdObgDetailsResponse>() {
                        },
                        "OBG details retrieved successfully");
            } else {
                log.info("No OBG details found for visit ID: {}", visitId);
                return ResponseUtils.createFailureResponse(null, new TypeReference<OpdObgDetailsResponse>() {
                        },
                        "No OBG examination details found for this visit", HttpStatus.NOT_FOUND.value());
            }
        } catch (Exception e) {
            log.error("Error fetching OBG details for visit ID: {}", visitId, e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<OpdObgDetailsResponse>() {
                    },
                    AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    /**
     * Map OpdObgDetailsProjection to OpdObgDetailsResponse
     * Converts projection data from database to response object
     *
     * @param projection the projection object from database query
     * @return mapped OpdObgDetailsResponse object
     */
    private OpdObgDetailsResponse mapProjectionToResponse(OpdObgDetailsProjection projection) {
        return OpdObgDetailsResponse.builder()
                .obgId(projection.getObgId())
                .patientId(projection.getPatientId())
                .visitId(projection.getVisitId())
                .opdDate(projection.getOpdDate())
                .obstetricHistoryNotes(projection.getObstetricHistoryNotes())
                .gravida(projection.getGravida())
                .para(projection.getPara())
                .abortions(projection.getAbortions())
                .livingChildren(projection.getLivingChildren())
                .conceptionType(projection.getConceptionType())
                .marriedLifeYears(projection.getMarriedLifeYears())
                .consanguinity(projection.getConsanguinity())
                .bookedStatus(projection.getBookedStatus())
                .immunisedStatus(projection.getImmunisedStatus())
                .trimester(projection.getTrimester())
                .gc(projection.getGc())
                .pallor(projection.getPallor())
                .peA(projection.getPedalEdema())
                .respiratorySystem(projection.getRespiratorySystem())
                .breathSounds(projection.getBreathSounds())
                .cardiovascularS1(projection.getCardiovascularS1())
                .cardiovascularS2(projection.getCardiovascularS2())
                .cardiovascularMurmurs(projection.getCardiovascularMurmurs())
                .ttStatus(projection.getTtStatus())
                .fhr(projection.getFhr())
                .presentation(projection.getPresentation())
                .palpationNotes(projection.getPalpationNotes())
                .pvDone(projection.getPvDone())
                .uterusHeight(projection.getUterusHeight())
                .uterusHeightSpecify(projection.getUterusHeightSpecify())
                .antenatalRemarks(projection.getAntenatalRemarks())
                .menarcheAge(projection.getMenarcheAge())
                .cycles(projection.getCycles())
                .rangeDays(projection.getRangeDays())
                .intervalDays(projection.getIntervalDays())
                .menstrualFlow(projection.getMenstrualFlow())
                .menstrualPause(projection.getMenstrualPause())
                .pvOsDilatation(projection.getPvOsDilatation())
                .pvEffacement(projection.getPvEffacement())
                .pvMembrane(projection.getPvMembrane())
                .pvLiquor(projection.getPvLiquor())
                .cervixConsistency(projection.getCervixConsistency())
                .cervixPosition(projection.getCervixPosition())
                .cervixLength(projection.getCervixLength())
                .stationPresenting(projection.getStationPresenting())
                .fetalHead(projection.getFetalHead())
                .pelvis(projection.getPelvis())
                .gynFlow(projection.getGynFlow())
                .gynMenarcheAge(projection.getGynMenarcheAge())
                .gynLastMenstrualPeriod(projection.getGynLastMenstrualPeriod())
                .gynMenstrualPattern(projection.getGynMenstrualPattern())
                .gynCycleType(projection.getGynCycleType())
                .sterilisation(projection.getSterilisation())
                .abdomenInspection(projection.getAbdomenInspection())
                .abdomenPalpation(projection.getAbdomenPalpation())
                .papSmearResult(projection.getPapSmearResult())
                .localExaminationNotes(projection.getLocalExaminationNotes())
                .perSpeculum(projection.getPerSpeculum())
                .bimanualExamination(projection.getBimanualExamination())
                .status(projection.getStatus())
                .lastUpdateDate(projection.getLastUpdateDate())
                .createdBy(projection.getCreatedBy())
                .lastUpdatedBy(projection.getLastUpdatedBy())
                .gynObstetricHistory(projection.getGynObstetricHistory())
                .build();
    }


}
