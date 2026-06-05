package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.OpdEntDetails;
import com.hims.entity.Patient;
import com.hims.entity.User;
import com.hims.entity.Visit;
import com.hims.entity.repository.OpdEntDetailsRepository;
import com.hims.entity.repository.OpdObgDetailsRepository;
import com.hims.entity.repository.PatientRepository;
import com.hims.entity.repository.VisitRepository;
import com.hims.request.OpdEntDetailsRequest;
import com.hims.response.ApiResponse;
import com.hims.service.OpdEntDetailsService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
@Slf4j
public class OpdEntDetailsServiceImpl implements OpdEntDetailsService {
    private final OpdEntDetailsRepository opdEntDetailsRepository;
    private final PatientRepository patientRepository;
    private final VisitRepository visitRepository;
    @Autowired
    private AuthUtil authUtil;

    @Override
    public ApiResponse<String> saveEntDetails(OpdEntDetailsRequest request) {
        try{
        User user = authUtil.getCurrentUser();

        Patient patient = patientRepository.findById(request.getPatientId()).orElseThrow(() -> new RuntimeException("Patient not found"));

        Visit visit = visitRepository.findById(request.getVisitId()).orElseThrow(() -> new RuntimeException("Visit not found"));


        OpdEntDetails entity = new OpdEntDetails();

        entity.setPatient(patient);
        entity.setVisit(visit);
        entity.setOpdDate(request.getOpdDate());

        entity.setRightPinna(request.getRightPinna());
        entity.setLeftPinna(request.getLeftPinna());
        entity.setRightEarCanal(request.getRightEarCanal());
        entity.setLeftEarCanal(request.getLeftEarCanal());
        entity.setRightTmStatus(request.getRightTmStatus());
        entity.setLeftTmStatus(request.getLeftTmStatus());

        entity.setRinneTest(request.getRinneTest());
        entity.setWeberTest(request.getWeberTest());
        entity.setAbcTest(request.getAbcTest());
        entity.setAudiometryFindings(request.getAudiometryFindings());

        entity.setExternalNose(request.getExternalNose());
        entity.setNasalMucosa(request.getNasalMucosa());
        entity.setSeptum(request.getSeptum());
        entity.setTurbinates(request.getTurbinates());
        entity.setNasalPolyp(request.getNasalPolyp());
        entity.setNasalDischarge(request.getNasalDischarge());
        entity.setMaxillaryTenderness(request.getMaxillaryTenderness());
        entity.setFrontalTenderness(request.getFrontalTenderness());

        entity.setOralCavity(request.getOralCavity());
        entity.setTonsilGrade(request.getTonsilGrade());
        entity.setTonsilCongestion(request.getTonsilCongestion());
        entity.setTonsilFollicles(request.getTonsilFollicles());
        entity.setTonsilMembrane(request.getTonsilMembrane());
        entity.setPeritonsillarAbscess(request.getPeritonsillarAbscess());

        entity.setPharynx(request.getPharynx());
        entity.setUvula(request.getUvula());
        entity.setVoiceQuality(request.getVoiceQuality());

        entity.setThyroidEnlargement(request.getThyroidEnlargement());
        entity.setCervicalNodes(request.getCervicalNodes());
        entity.setNeckMass(request.getNeckMass());
        entity.setNeckOtherFindings(request.getNeckOtherFindings());

        entity.setStatus(AppConstants.STATUS_Y.toLowerCase());
        entity.setCreatedBy(user.getFullName());
        entity.setLastUpdateDate(LocalDateTime.now());
        entity.setLastUpdatedBy(user.getFullName());

        opdEntDetailsRepository.save(entity);

        return ResponseUtils.createSuccessResponse("OPD Ent Details save successfully", new TypeReference<>() {
        });

    } catch (Exception e) {
        log.error("OPD Ent details field: ", e);
        return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());

    }
    }
}
