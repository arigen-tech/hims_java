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
import com.hims.projection.OpdEntDetailsProjection;
import com.hims.request.OpdEntDetailsRequest;
import com.hims.response.ApiResponse;
import com.hims.response.OpdEntDetailsResponse;
import com.hims.response.OphthalmologyExaminationDetailResponse;
import com.hims.service.OpdEntDetailsService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

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
        try {
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
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    },
                    AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());

        }
    }

    @Override
    public ApiResponse<OpdEntDetailsResponse> getEntDetailsByVisit(Long visitId) {
        try {
            Optional<OpdEntDetailsProjection> projection = opdEntDetailsRepository.getEntDetailsByVisitId(visitId);

            if (projection.isEmpty()) {
                return ResponseUtils.createSuccessResponse(null, new TypeReference<OpdEntDetailsResponse>() {
                });
            }

            OpdEntDetailsResponse response = convertToResponse(projection.get());

            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {
            });
        } catch (Exception e) {
            log.error("get Opd Ent Detail field: ", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    },
                    AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());

        }
    }

    private OpdEntDetailsResponse convertToResponse(OpdEntDetailsProjection p) {

        OpdEntDetailsResponse response = new OpdEntDetailsResponse();

        response.setEntId(p.getEntId());
        response.setPatientId(p.getPatientId());
        response.setVisitId(p.getVisitId());
        response.setOpdDate(p.getOpdDate());
        response.setRightPinna(p.getRightPinna());
        response.setLeftPinna(p.getLeftPinna());
        response.setRightEarCanal(p.getRightEarCanal());
        response.setLeftEarCanal(p.getLeftEarCanal());
        response.setRightTmStatus(p.getRightTmStatus());
        response.setLeftTmStatus(p.getLeftTmStatus());
        response.setRinneTest(p.getRinneTest());
        response.setWeberTest(p.getWeberTest());
        response.setAbcTest(p.getAbcTest());
        response.setAudiometryFindings(p.getAudiometryFindings());
        response.setExternalNose(p.getExternalNose());
        response.setNasalMucosa(p.getNasalMucosa());
        response.setSeptum(p.getSeptum());
        response.setTurbinates(p.getTurbinates());
        response.setNasalPolyp(p.getNasalPolyp());
        response.setNasalDischarge(p.getNasalDischarge());
        response.setMaxillaryTenderness(p.getMaxillaryTenderness());
        response.setFrontalTenderness(p.getFrontalTenderness());
        response.setOralCavity(p.getOralCavity());
        response.setTonsilGrade(p.getTonsilGrade());
        response.setTonsilCongestion(p.getTonsilCongestion());
        response.setTonsilFollicles(p.getTonsilFollicles());
        response.setTonsilMembrane(p.getTonsilMembrane());
        response.setPeritonsillarAbscess(p.getPeritonsillarAbscess());
        response.setPharynx(p.getPharynx());
        response.setUvula(p.getUvula());
        response.setVoiceQuality(p.getVoiceQuality());
        response.setThyroidEnlargement(p.getThyroidEnlargement());
        response.setCervicalNodes(p.getCervicalNodes());
        response.setNeckMass(p.getNeckMass());
        response.setNeckOtherFindings(p.getNeckOtherFindings());

        return response;
    }
}
