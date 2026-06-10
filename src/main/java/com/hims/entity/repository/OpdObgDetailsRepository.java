package com.hims.entity.repository;

import com.hims.entity.OpdObgDetails;
import com.hims.projection.OpdObgDetailsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OpdObgDetailsRepository extends JpaRepository<OpdObgDetails,Long> {

    /**
     * Fetch OBG details by visit ID using native SQL query with projection
     * Retrieves the most recent OBG examination record for a given visit
     *
     * @param visitId the visit ID to search for
     * @return Optional containing OBG details projection if found
     */
    @Query(value = """
SELECT
    obg_id AS obgId,
    patient_id AS patientId,
    visit_id AS visitId,
    opd_date AS opdDate,
    obstetric_history_notes AS obstetricHistoryNotes,
    gravida AS gravida,
    para AS para,
    abortions AS abortions,
    living_children AS livingChildren,
    conception_type AS conceptionType,
    married_life_years AS marriedLifeYears,
    consanguinity AS consanguinity,
    booked_status AS bookedStatus,
    immunised_status AS immunisedStatus,
    trimester AS trimester,
    gc AS gc,
    pallor AS pallor,
    pedal_edema AS pedalEdema,
    respiratory_system AS respiratorySystem,
    breath_sounds AS breathSounds,
    cardiovascular_s1 AS cardiovascularS1,
    cardiovascular_s2 AS cardiovascularS2,
    cardiovascular_murmurs AS cardiovascularMurmurs,
    tt_status AS ttStatus,
    fhr AS fhr,
    presentation AS presentation,
    palpation_notes AS palpationNotes,
    pv_done AS pvDone,
    uterus_height AS uterusHeight,
    uterus_height_specify AS uterusHeightSpecify,
    antenatal_remarks AS antenatalRemarks,
    menarche_age AS menarcheAge,
    cycles AS cycles,
    range_days AS rangeDays,
    interval_days AS intervalDays,
    menstrual_flow AS menstrualFlow,
    menstrual_pause AS menstrualPause,
    pv_os_dilatation AS pvOsDilatation,
    pv_effacement AS pvEffacement,
    pv_membrane AS pvMembrane,
    pv_liquor AS pvLiquor,
    cervix_consistency AS cervixConsistency,
    cervix_position AS cervixPosition,
    cervix_length AS cervixLength,
    station_presenting AS stationPresenting,
    fetal_head AS fetalHead,
    pelvis AS pelvis,
    gyn_flow AS gynFlow,
    gyn_menarche_age AS gynMenarcheAge,
    gyn_last_menstrual_period AS gynLastMenstrualPeriod,
    gyn_menstrual_pattern AS gynMenstrualPattern,
    gyn_cycle_type AS gynCycleType,
    sterilisation AS sterilisation,
    abdomen_inspection AS abdomenInspection,
    abdomen_palpation AS abdomenPalpation,
    pap_smear_result AS papSmearResult,
    local_examination_notes AS localExaminationNotes,
    per_speculum AS perSpeculum,
    gyn_obstetric_history AS gynObstetricHistory,
    bimanual_examination AS bimanualExamination,
    status AS status,
    last_update_date AS lastUpdateDate,
    created_by AS createdBy,
    last_updated_by AS lastUpdatedBy
FROM opd_obg_details
WHERE visit_id = :visitId
ORDER BY last_update_date DESC
LIMIT 1
""", nativeQuery = true)
    Optional<OpdObgDetailsProjection> findOpdObgDetailsByVisitId(@Param("visitId") Long visitId);
    /**
     * Find OBG details record (entity) by visit ID
     * @param visitId the visit ID
     * @return Optional containing OpdObgDetails entity if found
     */
    Optional<OpdObgDetails> findByVisitId(Long visitId);
}
