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

            Patient patient = patientRepository.findById(request.getPatientId()).orElseThrow(() -> new RuntimeException("Patient not found"));

            Visit visit = visitRepository.findById(request.getVisitId()).orElseThrow(() -> new RuntimeException("Visit not found"));

            OpdObgDetails entity = new OpdObgDetails();

            entity.setPatient(patient);
            entity.setVisit(visit);
            entity.setOpdDate(request.getOpdDate());
            entity.setObstetricHistoryNotes(request.getObstetricHistoryNotes());
            entity.setGravida(request.getGravida());
            entity.setPara(request.getPara());
            entity.setAbortions(request.getAbortions());
            entity.setLivingChildren(request.getLivingChildren());
            entity.setConceptionType(request.getConceptionType());
            entity.setMarriedLifeYears(request.getMarriedLifeYears());
            entity.setConsanguinity(request.getConsanguinity());
            entity.setBookedStatus(request.getBookedStatus());
            entity.setImmunisedStatus(request.getImmunisedStatus());
            entity.setTrimester(request.getTrimester());
            entity.setGc(request.getGc());
            entity.setPallor(request.getPallor());
            entity.setPedalEdema(request.getPedalEdema());
            entity.setRespiratorySystem(request.getRespiratorySystem());
            entity.setBreathSounds(request.getBreathSounds());
            entity.setCardiovascularS1(request.getCardiovascularS1());
            entity.setCardiovascularS2(request.getCardiovascularS2());
            entity.setCardiovascularMurmurs(request.getCardiovascularMurmurs());
            entity.setTtStatus(request.getTtStatus());
            entity.setFhr(request.getFhr());
            entity.setPresentation(request.getPresentation());
            entity.setPalpationNotes(request.getPalpationNotes());
            entity.setPvDone(request.getPvDone());
            entity.setUterusHeight(request.getUterusHeight());
            entity.setUterusHeightSpecify(request.getUterusHeightSpecify());
            entity.setAntenatalRemarks(request.getAntenatalRemarks());
            entity.setMenarcheAge(request.getMenarcheAge());
            entity.setCycles(request.getCycles());
            entity.setRangeDays(request.getRangeDays());
            entity.setIntervalDays(request.getIntervalDays());
            entity.setMenstrualFlow(request.getMenstrualFlow());
            entity.setMenstrualPause(request.getMenstrualPause());
            entity.setPvOsDilatation(request.getPvOsDilatation());
            entity.setPvEffacement(request.getPvEffacement());
            entity.setPvMembrane(request.getPvMembrane());
            entity.setPvLiquor(request.getPvLiquor());
            entity.setCervixConsistency(request.getCervixConsistency());
            entity.setCervixPosition(request.getCervixPosition());
            entity.setCervixLength(request.getCervixLength());
            entity.setStationPresenting(request.getStationPresenting());
            entity.setFetalHead(request.getFetalHead());
            entity.setPelvis(request.getPelvis());
            entity.setGynFlow(request.getGynFlow());
            entity.setGynMenarcheAge(request.getGynMenarcheAge());
            entity.setGynLastMenstrualPeriod(request.getGynLastMenstrualPeriod());
            entity.setGynMenstrualPattern(request.getGynMenstrualPattern());
            entity.setGynCycleType(request.getGynCycleType());
            entity.setSterilisation(request.getSterilisation());
            entity.setAbdomenInspection(request.getAbdomenInspection());
            entity.setAbdomenPalpation(request.getAbdomenPalpation());
            entity.setPapSmearResult(request.getPapSmearResult());
            entity.setLocalExaminationNotes(request.getLocalExaminationNotes());
            entity.setPerSpeculum(request.getPerSpeculum());
            entity.setBimanualExamination(request.getBimanualExamination());
            entity.setStatus(AppConstants.STATUS_Y.toLowerCase());
            entity.setCreatedBy(user.getFullName());
            entity.setLastUpdatedBy(user.getFullName());
            entity.setLastUpdateDate(LocalDateTime.now());

            opdObgDetailsRepository.save(entity);

            return ResponseUtils.createSuccessResponse("OPD Obg Details save successfully", new TypeReference<>() {
            });

        } catch (Exception e) {
            log.error("OPD Obg details field: ", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());

        }
    }
}
