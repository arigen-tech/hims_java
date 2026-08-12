package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.OpdOpthDetails;
import com.hims.entity.User;
import com.hims.entity.repository.OpdOpthDetailsRepository;
import com.hims.entity.repository.PatientRepository;
import com.hims.entity.repository.VisitRepository;
import com.hims.projection.OphthalmologyExaminationDetailProjection;
import com.hims.request.OpdOpthDetailsRequest;
import com.hims.request.OpdTemplateRequest;
import com.hims.response.ApiResponse;
import com.hims.response.OpdTemplateResponse;
import com.hims.response.OphthalmologyExaminationDetailResponse;
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
import java.util.Optional;

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
    public ApiResponse<String> opdVisionExaminationDetailsSaveOrUpdate(OpdOpthDetailsRequest req) {
        try {
            User user = authUtil.getCurrentUser();

            if (user == null) {
                return ResponseUtils.createFailureResponse(
                        null,
                        new TypeReference<>() {},
                        "Current user not found",
                        HttpStatus.NOT_FOUND.value()
                );
            }
            Optional<OpdOpthDetails> optional = repository.findByVisit_Id(req.getVisitId());
            OpdOpthDetails entity = optional.orElseGet(OpdOpthDetails::new);
            entity.setPatient(patientRepository.getReferenceById(req.getPatientId()));
            entity.setVisit(visitRepository.getReferenceById(req.getVisitId()));
            entity.setOpdDate(req.getOpdDate());

            // Distance Vision
            entity.setReDistanceUnaided(req.getReDistanceUnaided());
            entity.setReDistancePinhole(req.getReDistancePinhole());
            entity.setReDistanceBestCorrected(req.getReDistanceBestCorrected());
            entity.setLeDistanceUnaided(req.getLeDistanceUnaided());
            entity.setLeDistancePinhole(req.getLeDistancePinhole());
            entity.setLeDistanceBestCorrected(req.getLeDistanceBestCorrected());

            // Near Vision
            entity.setReNearUnaided(req.getReNearUnaided());
            entity.setReNearPinhole(req.getReNearPinhole());
            entity.setReNearBestCorrected(req.getReNearBestCorrected());
            entity.setLeNearUnaided(req.getLeNearUnaided());
            entity.setLeNearPinhole(req.getLeNearPinhole());
            entity.setLeNearBestCorrected(req.getLeNearBestCorrected());

            // Retinoscopy
            entity.setFundusGlow(req.getFundusGlow());
            entity.setReRetinoscopyAxis(req.getReRetinoscopyAxis());
            entity.setReRetinoscopyV(req.getReRetinoscopyV());
            entity.setReRetinoscopyH(req.getReRetinoscopyH());
            entity.setLeRetinoscopyAxis(req.getLeRetinoscopyAxis());
            entity.setLeRetinoscopyV(req.getLeRetinoscopyV());
            entity.setLeRetinoscopyH(req.getLeRetinoscopyH());

            // Right Eye
            entity.setReKeratometry(req.getReKeratometry());
            entity.setRePachymetry(req.getRePachymetry());
            entity.setReTonometry(req.getReTonometry());
            entity.setReFieldOfVision(req.getReFieldOfVision());
            entity.setReIolPower(req.getReIolPower());

            // Left Eye
            entity.setLeKeratometry(req.getLeKeratometry());
            entity.setLePachymetry(req.getLePachymetry());
            entity.setLeTonometry(req.getLeTonometry());
            entity.setLeFieldOfVision(req.getLeFieldOfVision());
            entity.setLeIolPower(req.getLeIolPower());

            // Distance Prescription
            entity.setReSphDist(req.getReSphDist());
            entity.setReCylDist(req.getReCylDist());
            entity.setReAxisDist(req.getReAxisDist());
            entity.setLeSphDist(req.getLeSphDist());
            entity.setLeCylDist(req.getLeCylDist());
            entity.setLeAxisDist(req.getLeAxisDist());

            // Near Prescription
            entity.setReSphNear(req.getReSphNear());
            entity.setReCylNear(req.getReCylNear());
            entity.setReAxisNear(req.getReAxisNear());
            entity.setLeSphNear(req.getLeSphNear());
            entity.setLeCylNear(req.getLeCylNear());
            entity.setLeAxisNear(req.getLeAxisNear());

            // Spectacle
            entity.setIpdValue(req.getIpdValue());
            entity.setSpectacleUse(req.getSpectacleUse());
            entity.setLensType(req.getLensType());

            // Right Eye Examination
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

            // Left Eye Examination
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

            // Right Eye Fundus
            entity.setReOpticDisc(req.getReOpticDisc());
            entity.setReFoveaMacula(req.getReFoveaMacula());
            entity.setReVitreousPosterior(req.getReVitreousPosterior());
            entity.setReBloodVessels(req.getReBloodVessels());
            entity.setReRetina(req.getReRetina());

            // Left Eye Fundus
            entity.setLeOpticDisc(req.getLeOpticDisc());
            entity.setLeFoveaMacula(req.getLeFoveaMacula());
            entity.setLeVitreousPosterior(req.getLeVitreousPosterior());
            entity.setLeBloodVessels(req.getLeBloodVessels());
            entity.setLeRetina(req.getLeRetina());

            // Common audit fields
            entity.setStatus(AppConstants.STATUS_Y.toLowerCase());
            entity.setLastUpdateDate(LocalDateTime.now());
            entity.setLastUpdatedBy(user.getFullName());
            entity.setReColourVision(req.getReColourVision());
            entity.setLeColourVision(req.getLeColourVision());

            // Set createdBy only for new record
            if (optional.isEmpty()) {
                entity.setCreatedBy(user.getFullName());
            }
            repository.save(entity);
            return ResponseUtils.createSuccessResponse(
                    "OPD Vision Examination details saved/updated successfully",
                    new TypeReference<>() {}
            );
        } catch (Exception e) {
            log.error("OPD Vision Examination details error: ", e);

            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    public ApiResponse<OphthalmologyExaminationDetailResponse> getOphthalmologyExaminationDetail(Long visitId) {

        try {
            Optional<OphthalmologyExaminationDetailProjection> optional = repository.getOphthalmologyExaminationDetail(visitId);

            // If no data found → return null (as you wanted)
            if (optional.isEmpty()) {return ResponseUtils.createSuccessResponse(null, new TypeReference<OphthalmologyExaminationDetailResponse>() {});
            }

            //  Convert projection → response
            OphthalmologyExaminationDetailResponse response = toResponse(optional.get());

            return ResponseUtils.createSuccessResponse(response, new TypeReference<OphthalmologyExaminationDetailResponse>() {}
            );

        } catch (Exception e) {
            log.error("getOphthalmologyExaminationDetail field: ", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());

        }
    }
    private OphthalmologyExaminationDetailResponse toResponse(OphthalmologyExaminationDetailProjection p) {
        if (p == null) return null;

        OphthalmologyExaminationDetailResponse r = new OphthalmologyExaminationDetailResponse();

        r.setPatientId(p.getPatientId());
        r.setVisitId(p.getVisitId());
        r.setOpdDate(p.getOpdDate());

        r.setReDistanceUnaided(p.getReDistanceUnaided());
        r.setReDistancePinhole(p.getReDistancePinhole());
        r.setReDistanceBestCorrected(p.getReDistanceBestCorrected());
        r.setLeDistanceUnaided(p.getLeDistanceUnaided());
        r.setLeDistancePinhole(p.getLeDistancePinhole());
        r.setLeDistanceBestCorrected(p.getLeDistanceBestCorrected());

        r.setReNearUnaided(p.getReNearUnaided());
        r.setReNearPinhole(p.getReNearPinhole());
        r.setReNearBestCorrected(p.getReNearBestCorrected());
        r.setLeNearUnaided(p.getLeNearUnaided());
        r.setLeNearPinhole(p.getLeNearPinhole());
        r.setLeNearBestCorrected(p.getLeNearBestCorrected());

        r.setFundusGlow(p.getFundusGlow());

        r.setReRetinoscopyAxis(p.getReRetinoscopyAxis());
        r.setReRetinoscopyV(p.getReRetinoscopyV());
        r.setReRetinoscopyH(p.getReRetinoscopyH());
        r.setLeRetinoscopyAxis(p.getLeRetinoscopyAxis());
        r.setLeRetinoscopyV(p.getLeRetinoscopyV());
        r.setLeRetinoscopyH(p.getLeRetinoscopyH());

        r.setReKeratometry(p.getReKeratometry());
        r.setRePachymetry(p.getRePachymetry());
        r.setReTonometry(p.getReTonometry());
        r.setReFieldOfVision(p.getReFieldOfVision());
        r.setReIolPower(p.getReIolPower());

        r.setLeKeratometry(p.getLeKeratometry());
        r.setLePachymetry(p.getLePachymetry());
        r.setLeTonometry(p.getLeTonometry());
        r.setLeFieldOfVision(p.getLeFieldOfVision());
        r.setLeIolPower(p.getLeIolPower());

        r.setReSphDist(p.getReSphDist());
        r.setReCylDist(p.getReCylDist());
        r.setReAxisDist(p.getReAxisDist());
        r.setLeSphDist(p.getLeSphDist());
        r.setLeCylDist(p.getLeCylDist());
        r.setLeAxisDist(p.getLeAxisDist());

        r.setReSphNear(p.getReSphNear());
        r.setReCylNear(p.getReCylNear());
        r.setReAxisNear(p.getReAxisNear());
        r.setLeSphNear(p.getLeSphNear());
        r.setLeCylNear(p.getLeCylNear());
        r.setLeAxisNear(p.getLeAxisNear());

        r.setIpdValue(p.getIpdValue());
        r.setSpectacleUse(p.getSpectacleUse());
        r.setLensType(p.getLensType());

        r.setReEyebrow(p.getReEyebrow());
        r.setReEyelid(p.getReEyelid());
        r.setReCornea(p.getReCornea());
        r.setReConjunctiva(p.getReConjunctiva());
        r.setReFornix(p.getReFornix());
        r.setReLimbus(p.getReLimbus());
        r.setReSclera(p.getReSclera());
        r.setReAnteriorChamber(p.getReAnteriorChamber());
        r.setReIris(p.getReIris());
        r.setRePupil(p.getRePupil());

        r.setLeEyebrow(p.getLeEyebrow());
        r.setLeEyelid(p.getLeEyelid());
        r.setLeCornea(p.getLeCornea());
        r.setLeConjunctiva(p.getLeConjunctiva());
        r.setLeFornix(p.getLeFornix());
        r.setLeLimbus(p.getLeLimbus());
        r.setLeSclera(p.getLeSclera());
        r.setLeAnteriorChamber(p.getLeAnteriorChamber());
        r.setLeIris(p.getLeIris());
        r.setLePupil(p.getLePupil());

        r.setReOpticDisc(p.getReOpticDisc());
        r.setReFoveaMacula(p.getReFoveaMacula());
        r.setReVitreousPosterior(p.getReVitreousPosterior());
        r.setReBloodVessels(p.getReBloodVessels());
        r.setReRetina(p.getReRetina());

        r.setLeOpticDisc(p.getLeOpticDisc());
        r.setLeFoveaMacula(p.getLeFoveaMacula());
        r.setLeVitreousPosterior(p.getLeVitreousPosterior());
        r.setLeBloodVessels(p.getLeBloodVessels());
        r.setLeRetina(p.getLeRetina());
        r.setReColourVision(p.getReColourVision());
        r.setLeColourVision(p.getLeColourVision());

        return r;
    }
}