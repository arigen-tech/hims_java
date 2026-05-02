package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.OpdOpthDetails;
import com.hims.entity.User;
import com.hims.entity.repository.OpdOpthDetailsRepository;
import com.hims.entity.repository.PatientRepository;
import com.hims.entity.repository.VisitRepository;
import com.hims.request.OpdOpthDetailsRequest;
import com.hims.request.OpdTemplateRequest;
import com.hims.response.ApiResponse;
import com.hims.response.OpdTemplateResponse;
import com.hims.service.OpdOpthDetailsService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpdOpthDetailsImpl implements OpdOpthDetailsService {
    private final OpdOpthDetailsRepository repository;
    private final PatientRepository patientRepository;
    private final VisitRepository visitRepository;
    @Autowired
    private AuthUtil authUtil;

    @Override
    @Transactional
    public ApiResponse<String> opdVisionExaminationDetailsSave(OpdOpthDetailsRequest req) {
        try {
            User user = authUtil.getCurrentUser();
            if (user == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                        },
                        "Current user not found", HttpStatus.NOT_FOUND.value());
            }
            OpdOpthDetails entity = new OpdOpthDetails();

            entity.setPatient(patientRepository.getReferenceById(req.getPatientId()));
            entity.setVisit(visitRepository.getReferenceById(req.getVisitId()));
            entity.setOpdDate(req.getOpdDate());

            entity.setReDistanceUnaided(req.getReDistanceUnaided());
            entity.setReDistancePinhole(req.getReDistancePinhole());
            entity.setReDistanceBestCorrected(req.getReDistanceBestCorrected());
            entity.setLeDistanceUnaided(req.getLeDistanceUnaided());
            entity.setLeDistancePinhole(req.getLeDistancePinhole());
            entity.setLeDistanceBestCorrected(req.getLeDistanceBestCorrected());

            entity.setReNearUnaided(req.getReNearUnaided());
            entity.setReNearPinhole(req.getReNearPinhole());
            entity.setReNearBestCorrected(req.getReNearBestCorrected());
            entity.setLeNearUnaided(req.getLeNearUnaided());
            entity.setLeNearPinhole(req.getLeNearPinhole());
            entity.setLeNearBestCorrected(req.getLeNearBestCorrected());

            entity.setFundusGlow(req.getFundusGlow());
            entity.setReRetinoscopyAxis(req.getReRetinoscopyAxis());
            entity.setReRetinoscopyV(req.getReRetinoscopyV());
            entity.setReRetinoscopyH(req.getReRetinoscopyH());
            entity.setLeRetinoscopyAxis(req.getLeRetinoscopyAxis());
            entity.setLeRetinoscopyV(req.getLeRetinoscopyV());
            entity.setLeRetinoscopyH(req.getLeRetinoscopyH());

            entity.setReKeratometry(req.getReKeratometry());
            entity.setRePachymetry(req.getRePachymetry());
            entity.setReTonometry(req.getReTonometry());
            entity.setReFieldOfVision(req.getReFieldOfVision());
            entity.setReIolPower(req.getReIolPower());

            entity.setLeKeratometry(req.getLeKeratometry());
            entity.setLePachymetry(req.getLePachymetry());
            entity.setLeTonometry(req.getLeTonometry());
            entity.setLeFieldOfVision(req.getLeFieldOfVision());
            entity.setLeIolPower(req.getLeIolPower());

            entity.setReSphDist(req.getReSphDist());
            entity.setReCylDist(req.getReCylDist());
            entity.setReAxisDist(req.getReAxisDist());
            entity.setLeSphDist(req.getLeSphDist());
            entity.setLeCylDist(req.getLeCylDist());
            entity.setLeAxisDist(req.getLeAxisDist());

            entity.setReSphNear(req.getReSphNear());
            entity.setReCylNear(req.getReCylNear());
            entity.setReAxisNear(req.getReAxisNear());
            entity.setLeSphNear(req.getLeSphNear());
            entity.setLeCylNear(req.getLeCylNear());
            entity.setLeAxisNear(req.getLeAxisNear());

            entity.setIpdValue(req.getIpdValue());
            entity.setSpectacleUse(req.getSpectacleUse());
            entity.setLensType(req.getLensType());

            entity.setReEyebrow(req.getReEyebrow());
            entity.setReEyelid(req.getReEyelid());
            entity.setReCornea(req.getReCornea());
            entity.setReConjunctiva(req.getReConjunctiva());
            entity.setReFornix(req.getReFornix());
            entity.setReLimbus(req.getReLimbus());
            entity.setReSclera(req.getReSclera());
            entity.setReAnteriorChamber(req.getReAnteriorChamber());
            entity.setReIris(req.getReIris());
            entity.setRePupil(req.getRePupil());

            entity.setLeEyebrow(req.getLeEyebrow());
            entity.setLeEyelid(req.getLeEyelid());
            entity.setLeCornea(req.getLeCornea());
            entity.setLeConjunctiva(req.getLeConjunctiva());
            entity.setLeFornix(req.getLeFornix());
            entity.setLeLimbus(req.getLeLimbus());
            entity.setLeSclera(req.getLeSclera());
            entity.setLeAnteriorChamber(req.getLeAnteriorChamber());
            entity.setLeIris(req.getLeIris());
            entity.setLePupil(req.getLePupil());


            entity.setReOpticDisc(req.getReOpticDisc());
            entity.setReFoveaMacula(req.getReFoveaMacula());
            entity.setReVitreousPosterior(req.getReVitreousPosterior());
            entity.setReBloodVessels(req.getReBloodVessels());
            entity.setReRetina(req.getReRetina());

            entity.setLeOpticDisc(req.getLeOpticDisc());
            entity.setLeFoveaMacula(req.getLeFoveaMacula());
            entity.setLeVitreousPosterior(req.getLeVitreousPosterior());
            entity.setLeBloodVessels(req.getLeBloodVessels());
            entity.setLeRetina(req.getLeRetina());

            entity.setStatus(AppConstants.STATUS_Y.toLowerCase());
            entity.setLastUpdateDate(LocalDateTime.now());
            entity.setCreatedBy(user.getFullName());
            entity.setLastUpdatedBy(user.getFullName());

            repository.save(entity);
            return ResponseUtils.createSuccessResponse(
                    "OPD Vision Examination details save successfully", new TypeReference<>() {
                    });
        } catch (Exception e) {
            log.error("OPD Vision Examination details field: ", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());

        }
    }  }