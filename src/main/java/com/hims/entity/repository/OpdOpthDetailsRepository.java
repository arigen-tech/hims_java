package com.hims.entity.repository;

import com.hims.entity.OpdOpthDetails;
import com.hims.projection.OphthalmologyExaminationDetailProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OpdOpthDetailsRepository extends JpaRepository<OpdOpthDetails,Long> {
    @Query("""
SELECT 
    p.id AS patientId,
    v.id AS visitId,
    o.opdDate AS opdDate,

    o.reDistanceUnaided AS reDistanceUnaided,
    o.reDistancePinhole AS reDistancePinhole,
    o.reDistanceBestCorrected AS reDistanceBestCorrected,
    o.leDistanceUnaided AS leDistanceUnaided,
    o.leDistancePinhole AS leDistancePinhole,
    o.leDistanceBestCorrected AS leDistanceBestCorrected,

    o.reNearUnaided AS reNearUnaided,
    o.reNearPinhole AS reNearPinhole,
    o.reNearBestCorrected AS reNearBestCorrected,
    o.leNearUnaided AS leNearUnaided,
    o.leNearPinhole AS leNearPinhole,
    o.leNearBestCorrected AS leNearBestCorrected,

    o.fundusGlow AS fundusGlow,

    o.reRetinoscopyAxis AS reRetinoscopyAxis,
    o.reRetinoscopyV AS reRetinoscopyV,
    o.reRetinoscopyH AS reRetinoscopyH,
    o.leRetinoscopyAxis AS leRetinoscopyAxis,
    o.leRetinoscopyV AS leRetinoscopyV,
    o.leRetinoscopyH AS leRetinoscopyH,

    o.reKeratometry AS reKeratometry,
    o.rePachymetry AS rePachymetry,
    o.reTonometry AS reTonometry,
    o.reFieldOfVision AS reFieldOfVision,
    o.reIolPower AS reIolPower,

    o.leKeratometry AS leKeratometry,
    o.lePachymetry AS lePachymetry,
    o.leTonometry AS leTonometry,
    o.leFieldOfVision AS leFieldOfVision,
    o.leIolPower AS leIolPower,

    o.reSphDist AS reSphDist,
    o.reCylDist AS reCylDist,
    o.reAxisDist AS reAxisDist,
    o.leSphDist AS leSphDist,
    o.leCylDist AS leCylDist,
    o.leAxisDist AS leAxisDist,

    o.reSphNear AS reSphNear,
    o.reCylNear AS reCylNear,
    o.reAxisNear AS reAxisNear,
    o.leSphNear AS leSphNear,
    o.leCylNear AS leCylNear,
    o.leAxisNear AS leAxisNear,

    o.ipdValue AS ipdValue,
    o.spectacleUse AS spectacleUse,
    o.lensType AS lensType,

    o.reEyebrow AS reEyebrow,
    o.reEyelid AS reEyelid,
    o.reCornea AS reCornea,
    o.reConjunctiva AS reConjunctiva,
    o.reFornix AS reFornix,
    o.reLimbus AS reLimbus,
    o.reSclera AS reSclera,
    o.reAnteriorChamber AS reAnteriorChamber,
    o.reIris AS reIris,
    o.rePupil AS rePupil,

    o.leEyebrow AS leEyebrow,
    o.leEyelid AS leEyelid,
    o.leCornea AS leCornea,
    o.leConjunctiva AS leConjunctiva,
    o.leFornix AS leFornix,
    o.leLimbus AS leLimbus,
    o.leSclera AS leSclera,
    o.leAnteriorChamber AS leAnteriorChamber,
    o.leIris AS leIris,
    o.lePupil AS lePupil,

    o.reOpticDisc AS reOpticDisc,
    o.reFoveaMacula AS reFoveaMacula,
    o.reVitreousPosterior AS reVitreousPosterior,
    o.reBloodVessels AS reBloodVessels,
    o.reRetina AS reRetina,

    o.leOpticDisc AS leOpticDisc,
    o.leFoveaMacula AS leFoveaMacula,
    o.leVitreousPosterior AS leVitreousPosterior,
    o.leBloodVessels AS leBloodVessels,
    o.leRetina AS leRetina

FROM OpdOpthDetails o
JOIN o.visit v
JOIN v.patient p  
WHERE v.id = :visitId

""")
    Optional<OphthalmologyExaminationDetailProjection> getOphthalmologyExaminationDetail(
            @Param("visitId") Long visitId);

    Optional<OpdOpthDetails> findByVisit_Id(Long visitId);
}
