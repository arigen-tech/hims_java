package com.hims.entity.repository;

import com.hims.entity.ProcedureDt;
import com.hims.projection.ProcedureWorklistProjection;
import com.hims.response.ProcedureWorklistResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcedureDtRepository extends JpaRepository<ProcedureDt, Long> {

    @Query(value = """
            SELECT
                ph.procedure_hd_id AS procedureHdId,
                pd.procedure_dt_id AS procedureDtId,
            
                p.patient_id AS patientId,
                p.p_mobile_number AS mobileNo,
            
                CONCAT_WS(
                    ' ',
                    p.p_fn,
                    p.p_mn,
                    p.p_ln
                ) AS patientName,
            
                CAST(
                    EXTRACT(
                        YEAR FROM AGE(CURRENT_DATE, p.p_dob)
                    ) AS INTEGER
                ) AS age,
            
                mg.gender_name AS gender,
            
                d.department_name AS department,
            
                mp.procedure_name AS procedure,
            
                pd.completed_session_count AS completedSessions,
                pd.planned_session_count AS totalSessions,
            
                (
                    SELECT MIN(ps.scheduled_date_time)
                    FROM procedure_session ps
                    WHERE ps.procedure_dt_id = pd.procedure_dt_id
                      AND ps.status = :sessionStatus
                ) AS scheduledDateTime,
            
                ph.advised_by AS advisedBy,
            
                pd.billing_status AS billingStatus
            
            FROM procedure_dt pd
            
            INNER JOIN procedure_hd ph
                ON ph.procedure_hd_id = pd.procedure_hd_id
            
            INNER JOIN patient p
                ON p.patient_id = ph.patient_id
            
            LEFT JOIN mas_gender mg
                ON mg.id = p.p_gender_id
            
            INNER JOIN mas_department d
                ON d.department_id = ph.department_id
            
            INNER JOIN mas_procedure mp
                ON mp.procedure_id = pd.procedure_id
            
            WHERE
                ph.status = :procedureHdStatus
                AND pd.status = :procedureDtStatus
            
                AND (
                    :mobileNo IS NULL
                    OR :mobileNo = ''
                    OR p.p_mobile_number LIKE CONCAT('%', :mobileNo, '%')
                )
            
                AND (
                    :patientName IS NULL
                    OR :patientName = ''
                    OR LOWER(
                        CONCAT_WS(
                            ' ',
                            p.p_fn,
                            p.p_mn,
                            p.p_ln
                        )
                    ) LIKE LOWER(
                        CONCAT('%', :patientName, '%')
                    )
                )
            
            ORDER BY
                scheduledDateTime ASC
            """,

            countQuery = """
                    SELECT COUNT(*)
                    
                    FROM procedure_dt pd
                    
                    INNER JOIN procedure_hd ph
                        ON ph.procedure_hd_id = pd.procedure_hd_id
                    
                    INNER JOIN patient p
                        ON p.patient_id = ph.patient_id
                    
                    INNER JOIN mas_department d
                        ON d.department_id = ph.department_id
                    
                    INNER JOIN mas_procedure mp
                        ON mp.procedure_id = pd.procedure_id
                    
                    WHERE
                        ph.status = :procedureHdStatus
                        AND pd.status = :procedureDtStatus
                    
                        AND (
                            :mobileNo IS NULL
                            OR :mobileNo = ''
                            OR p.p_mobile_number LIKE CONCAT('%', :mobileNo, '%')
                        )
                    
                        AND (
                            :patientName IS NULL
                            OR :patientName = ''
                            OR LOWER(
                                CONCAT_WS(
                                    ' ',
                                    p.p_fn,
                                    p.p_mn,
                                    p.p_ln
                                )
                            ) LIKE LOWER(
                                CONCAT('%', :patientName, '%')
                            )
                        )
                    """,

            nativeQuery = true)
    Page<ProcedureWorklistProjection> getProcedureWorklist(@Param("mobileNo") String mobileNo, @Param("patientName") String patientName, @Param("procedureHdStatus") String procedureHdStatus, @Param("procedureDtStatus") String procedureDtStatus, @Param("sessionStatus") String sessionStatus, Pageable pageable);

    List<ProcedureDt> findByProcedureHd_ProcedureHdIdAndStatusOrderBySequenceNoAsc(Long procedureHdId, String status);
}