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
import com.hims.request.OpdObgDetailsRequest;
import com.hims.response.ApiResponse;
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
    public ApiResponse<String> saveObgDetails(OpdObgDetailsRequest request) {
        try {
            User user = authUtil.getCurrentUser();
            Patient patient = patientRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new RuntimeException("Patient not found"));
            Visit visit = visitRepository.findById(request.getVisitId())
                    .orElseThrow(() -> new RuntimeException("Visit not found"));

            OpdObgDetails entity = new OpdObgDetails();

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
                entity.setObstetricHistoryNotes(gh.getObstetricHistory());

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
                entity.setPvDone(obg.getPerExamination());
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
            entity.setCreatedBy(user.getFullName());
            entity.setLastUpdatedBy(user.getFullName());
            entity.setLastUpdateDate(LocalDateTime.now());

            opdObgDetailsRepository.save(entity);

            return ResponseUtils.createSuccessResponse("OPD Obg Details saved successfully", new TypeReference<>() {});

        } catch (Exception e) {
            log.error("OPD Obg details field: ", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
}
