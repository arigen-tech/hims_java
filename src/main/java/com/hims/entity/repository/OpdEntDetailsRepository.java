package com.hims.entity.repository;

import com.hims.entity.OpdEntDetails;
import com.hims.projection.OpdEntDetailsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OpdEntDetailsRepository extends JpaRepository<OpdEntDetails,Long> {
    @Query(value = """
            SELECT
                ent_id AS entId,
                patient_id AS patientId,
                visit_id AS visitId,
                opd_date AS opdDate,
                right_pinna AS rightPinna,
                left_pinna AS leftPinna,
                right_ear_canal AS rightEarCanal,
                left_ear_canal AS leftEarCanal,
                right_tm_status AS rightTmStatus,
                left_tm_status AS leftTmStatus,
                rinne_test AS rinneTest,
                weber_test AS weberTest,
                abc_test AS abcTest,
                audiometry_findings AS audiometryFindings,
                external_nose AS externalNose,
                nasal_mucosa AS nasalMucosa,
                septum AS septum,
                turbinates AS turbinates,
                nasal_polyp AS nasalPolyp,
                nasal_discharge AS nasalDischarge,
                maxillary_tenderness AS maxillaryTenderness,
                frontal_tenderness AS frontalTenderness,
                oral_cavity AS oralCavity,
                tonsil_grade AS tonsilGrade,
                tonsil_congestion AS tonsilCongestion,
                tonsil_follicles AS tonsilFollicles,
                tonsil_membrane AS tonsilMembrane,
                peritonsillar_abscess AS peritonsillarAbscess,
                pharynx AS pharynx,
                uvula AS uvula,
                voice_quality AS voiceQuality,
                thyroid_enlargement AS thyroidEnlargement,
                cervical_nodes AS cervicalNodes,
                neck_mass AS neckMass,
                neck_other_findings AS neckOtherFindings
            FROM opd_ent_details
            WHERE visit_id = :visitId
            """,
            nativeQuery = true)
    Optional<OpdEntDetailsProjection> getEntDetailsByVisitId(
            @Param("visitId") Long visitId);
}
