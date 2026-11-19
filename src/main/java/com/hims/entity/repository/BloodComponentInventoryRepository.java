package com.hims.entity.repository;

import com.hims.entity.BloodComponentInventory;
import com.hims.projection.*;
import com.hims.response.BloodComponentExpiryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BloodComponentInventoryRepository extends JpaRepository<BloodComponentInventory,Long> {
    @Query(
            value = """
        SELECT 
            bg.blood_group_code AS bloodGroup,

            COUNT(bci.inventory_id) AS totalUnits,

            SUM(CASE WHEN LOWER(bc.component_code) = :component_prbc THEN 1 ELSE 0 END) AS prbc,
            SUM(CASE WHEN LOWER(bc.component_code) = :component_plt THEN 1 ELSE 0 END) AS platelets,
            SUM(CASE WHEN LOWER(bc.component_code) = :component_plasma THEN 1 ELSE 0 END) AS plasma,
            SUM(CASE WHEN LOWER(bc.component_code) = :component_cryo THEN 1 ELSE 0 END) AS cryo

        FROM blood_component_inventory bci
        JOIN mas_blood_group bg
            ON bg.blood_group_id = bci.blood_group_id
        JOIN mas_blood_component bc
            ON bc.component_id = bci.component_id

        WHERE bci.hospital_id = :hospitalId
          AND (:bloodGroupId IS NULL OR bci.blood_group_id = :bloodGroupId)
          AND (:componentId IS NULL OR bci.component_id = :componentId)
          AND (:inventoryStatus IS NULL OR bci.inventory_status = :inventoryStatus)
          AND (:collectionType IS NULL)
          AND (
              :expiryFilter IS NULL
              OR (
                  bci.expiry_date >= CURRENT_TIMESTAMP
                  AND bci.expiry_date < CURRENT_TIMESTAMP +
                      (
                          CAST(
                              regexp_replace(:expiryFilter, '[^0-9]', '', 'g')
                              AS INTEGER
                          )
                          *
                          CASE
                              WHEN LOWER(:expiryFilter) LIKE '%hr%'
                              THEN INTERVAL '1 hour'
                              ELSE INTERVAL '1 day'
                          END
                      )
              )
          )

        GROUP BY bg.blood_group_code
        """,

            countQuery = """
        SELECT COUNT(*)
        FROM (
            SELECT bg.blood_group_code
            FROM blood_component_inventory bci
            JOIN mas_blood_group bg
                ON bg.blood_group_id = bci.blood_group_id
            JOIN mas_blood_component bc
                ON bc.component_id = bci.component_id

            WHERE bci.hospital_id = :hospitalId
              AND (:bloodGroupId IS NULL OR bci.blood_group_id = :bloodGroupId)
              AND (:componentId IS NULL OR bci.component_id = :componentId)
              AND (:inventoryStatus IS NULL OR bci.inventory_status = :inventoryStatus)
              AND (:collectionType IS NULL)
              AND (
                  :expiryFilter IS NULL
                  OR (
                      bci.expiry_date >= CURRENT_TIMESTAMP
                      AND bci.expiry_date < CURRENT_TIMESTAMP +
                          (
                              CAST(
                                  regexp_replace(:expiryFilter, '[^0-9]', '', 'g')
                                  AS INTEGER
                              )
                              *
                              CASE
                                  WHEN LOWER(:expiryFilter) LIKE '%hr%'
                                  THEN INTERVAL '1 hour'
                                  ELSE INTERVAL '1 day'
                          END
                          )
                  )
              )

            GROUP BY bg.blood_group_code
        ) x
        """,
            nativeQuery = true
    )
    Page<BloodStockSummaryProjection> getSummary(
            @Param("bloodGroupId") Long bloodGroupId,
            @Param("componentId") Long componentId,
            @Param("inventoryStatus") Long inventoryStatus,
            @Param("collectionType") Long collectionType,
            @Param("expiryFilter") String expiryFilter,
            @Param("hospitalId") Long hospitalId,
            @Param("component_cryo") String component_cryo,
            @Param("component_plasma") String component_plasma,
            @Param("component_plt") String component_plt,
            @Param("component_prbc") String component_prbc,
            Pageable pageable
    );

    @Query(
            value = """
        SELECT
            bci.unit_no AS unitNo,
            bc.component_code AS component,
            bg.blood_group_code AS bloodGroup,
            bci.volume_ml AS volumeMl,
            bci.expiry_date AS expiryDate,
            ms.status_code AS status,

            CASE
                WHEN bci.reserved_for_patient_id IS NOT NULL
                     AND bci.reserved_for_inpatient_id IS NOT NULL
                THEN CONCAT_WS(
                    ' / ',
                    TRIM(CONCAT_WS(' ', p.p_fn, p.p_mn, p.p_ln)),
                    i.admission_no
                )

                WHEN bci.reserved_for_patient_id IS NOT NULL
                THEN TRIM(CONCAT_WS(' ', p.p_fn, p.p_mn, p.p_ln))

                WHEN bci.reserved_for_inpatient_id IS NOT NULL
                THEN CONCAT_WS(
                    ' / ',
                    TRIM(CONCAT_WS(' ', ip.p_fn, ip.p_mn, ip.p_ln)),
                    i.admission_no
                )

                ELSE NULL
            END AS reservedFor

        FROM blood_component_inventory bci

        JOIN mas_blood_component bc
            ON bc.component_id = bci.component_id

        JOIN mas_blood_group bg
            ON bg.blood_group_id = bci.blood_group_id

        JOIN mas_blood_inventory_status ms
            ON ms.inventory_status_id = bci.inventory_status

        LEFT JOIN patient p
            ON p.patient_id = bci.reserved_for_patient_id

        LEFT JOIN inpatient i
            ON i.inpatient_id = bci.reserved_for_inpatient_id

        LEFT JOIN patient ip
            ON ip.patient_id = i.patient

        WHERE
            bci.hospital_id = :hospitalId

            AND (:bloodGroupId IS NULL
                 OR bci.blood_group_id = :bloodGroupId)

            AND (:componentId IS NULL
                 OR bci.component_id = :componentId)

            AND (:inventoryStatus IS NULL
                 OR bci.inventory_status = :inventoryStatus)

            AND bci.expiry_date >= CURRENT_TIMESTAMP

            AND (
                :expiryFilter IS NULL
                OR bci.expiry_date < CURRENT_TIMESTAMP +
                    (
                        CAST(
                            regexp_replace(
                                :expiryFilter,
                                '[^0-9]',
                                '',
                                'g'
                            ) AS INTEGER
                        )
                        *
                        CASE
                            WHEN LOWER(:expiryFilter) LIKE '%hr%'
                            THEN INTERVAL '1 hour'
                            ELSE INTERVAL '1 day'
                        END
                    )
            )

        ORDER BY bci.expiry_date ASC
        """,

            countQuery = """
        SELECT COUNT(*)
        FROM blood_component_inventory bci
        WHERE
            bci.hospital_id = :hospitalId

            AND (:bloodGroupId IS NULL
                 OR bci.blood_group_id = :bloodGroupId)

            AND (:componentId IS NULL
                 OR bci.component_id = :componentId)

            AND (:inventoryStatus IS NULL
                 OR bci.inventory_status = :inventoryStatus)

            AND bci.expiry_date >= CURRENT_TIMESTAMP

            AND (
                :expiryFilter IS NULL
                OR bci.expiry_date < CURRENT_TIMESTAMP +
                    (
                        CAST(
                            regexp_replace(
                                :expiryFilter,
                                '[^0-9]',
                                '',
                                'g'
                            ) AS INTEGER
                        )
                        *
                        CASE
                            WHEN LOWER(:expiryFilter) LIKE '%hr%'
                            THEN INTERVAL '1 hour'
                            ELSE INTERVAL '1 day'
                        END
                    )
            )
        """,
            nativeQuery = true
    )
    Page<BloodStockDetailedProjection> getDetailed(
            @Param("bloodGroupId") Long bloodGroupId,
            @Param("componentId") Long componentId,
            @Param("inventoryStatus") Long inventoryStatus,
            @Param("expiryFilter") String expiryFilter,
            @Param("hospitalId") Long hospitalId,
            Pageable pageable
    );

    @Query(value = """
    SELECT
        bci.inventory_id AS inventoryId,
        bci.unit_no AS unitNo,
        bci.blood_group_id AS bloodGroupId,
        bci.volume_ml AS volumeMl,
        bci.expiry_date AS expiryDate,
        bci.component_id AS componentId,
        c.is_preferred AS preferred
    FROM blood_component_inventory bci
    JOIN mas_blood_compatibility c
        ON c.donor_blood_group_id = bci.blood_group_id
    WHERE c.patient_blood_group_id = :patientBloodGroupId
      AND c.component_id = :componentId
      AND LOWER(c.status) = LOWER(:status)
      AND bci.component_id = :componentId
      AND bci.inventory_status = :inventoryStatus
      AND bci.expiry_date > CURRENT_TIMESTAMP
      AND bci.reserved_for_patient_id IS NULL
      AND bci.reserved_for_inpatient_id IS NULL
      AND bci.issue_datetime IS NULL
    ORDER BY
        CASE
            WHEN LOWER(c.is_preferred) = :status THEN 0
            ELSE 1
        END,
        bci.expiry_date ASC
    """, nativeQuery = true)
    List<BloodInventoryProjection> findAvailableBloodInventory(
            @Param("patientBloodGroupId") Long patientBloodGroupId,
            @Param("componentId") Long componentId,
            @Param("status") String status,
            @Param("inventoryStatus") Long inventoryStatus
    );
    @Query(
            value = """
        SELECT
            mbc.component_code AS componentCode,
            mbg.blood_group_name AS bloodGroupName,

            COUNT(CASE
                WHEN bci.inventory_status = :availableStatusId
                THEN 1
            END) AS availableUnits,

            COUNT(CASE
                WHEN bci.inventory_status = :reservedStatusId
                THEN 1
            END) AS reservedUnits,

            COUNT(CASE
                WHEN bci.inventory_status = :allocatedStatusId
                THEN 1
            END) AS allocatedUnits,

            COUNT(CASE
                WHEN bci.inventory_status = :expiredStatusId
                THEN 1
            END) AS expiredUnits

        FROM blood_component_inventory bci

        INNER JOIN mas_blood_group mbg
            ON bci.blood_group_id = mbg.blood_group_id

        INNER JOIN mas_blood_component mbc
            ON bci.component_id = mbc.component_id

        WHERE
            (:bloodGroupId IS NULL OR bci.blood_group_id = :bloodGroupId)

            AND (:componentId IS NULL OR bci.component_id = :componentId)

            AND (:filterStatusId IS NULL OR bci.inventory_status = :filterStatusId)

            AND (:fromDate IS NULL OR bci.expiry_date >= :fromDate)

            AND (:toDate IS NULL OR bci.expiry_date <= :toDate)

        GROUP BY
            mbc.component_code,
            mbg.blood_group_name

        ORDER BY
            mbc.component_code,
            mbg.blood_group_name
        """,
            countQuery = """
        SELECT COUNT(*)
        FROM (
            SELECT 1
            FROM blood_component_inventory bci

            INNER JOIN mas_blood_group mbg
                ON bci.blood_group_id = mbg.blood_group_id

            INNER JOIN mas_blood_component mbc
                ON bci.component_id = mbc.component_id

            WHERE
                (:bloodGroupId IS NULL OR bci.blood_group_id = :bloodGroupId)

                AND (:componentId IS NULL OR bci.component_id = :componentId)

                AND (:filterStatusId IS NULL OR bci.inventory_status = :filterStatusId)

                AND (:fromDate IS NULL OR bci.expiry_date >= :fromDate)

                AND (:toDate IS NULL OR bci.expiry_date <= :toDate)

            GROUP BY
                mbc.component_code,
                mbg.blood_group_name
        ) report_count
        """,
            nativeQuery = true
    )
    Page<ComponentWiseStockProjection> getComponentWiseStockReport(

            @Param("componentId") Long componentId,

            @Param("bloodGroupId") Long bloodGroupId,

            @Param("filterStatusId") Long filterStatusId,

            @Param("availableStatusId") Long availableStatusId,

            @Param("reservedStatusId") Long reservedStatusId,

            @Param("allocatedStatusId") Long allocatedStatusId,

            @Param("expiredStatusId") Long expiredStatusId,

            @Param("fromDate") LocalDate fromDate,

            @Param("toDate") LocalDate toDate,

            Pageable pageable
    );


    @Query(
            value = """
        SELECT
            bci.inventory_id AS "inventoryId",
            bci.unit_no AS "unitNo",
            mbc.component_code AS "componentCode",
            mbc.component_name AS "componentName",
            mbg.blood_group_name AS "bloodGroup",
            bci.expiry_date AS "expiryDate",
            (bci.expiry_date - CURRENT_DATE) AS "daysLeft",
            mbis.status_code AS "inventoryStatus"

        FROM blood_component_inventory bci

        LEFT JOIN mas_blood_component mbc
            ON mbc.component_id = bci.component_id

        LEFT JOIN mas_blood_group mbg
            ON mbg.blood_group_id = bci.blood_group_id

        LEFT JOIN mas_blood_inventory_status mbis
            ON mbis.inventory_status_id = bci.inventory_status

        WHERE
            (CAST(:expiryFromDate AS date) IS NULL
                OR bci.expiry_date >= CAST(:expiryFromDate AS date))

            AND (CAST(:expiryToDate AS date) IS NULL
                OR bci.expiry_date <= CAST(:expiryToDate AS date))

            AND (CAST(:componentId AS bigint) IS NULL
                OR bci.component_id = CAST(:componentId AS bigint))

            AND (CAST(:bloodGroupId AS bigint) IS NULL
                OR bci.blood_group_id = CAST(:bloodGroupId AS bigint))

            AND (CAST(:inventoryStatus AS bigint) IS NULL
                OR bci.inventory_status = CAST(:inventoryStatus AS bigint))

        ORDER BY bci.expiry_date ASC
        """,

            countQuery = """
        SELECT COUNT(*)
        FROM blood_component_inventory bci

        WHERE
            (CAST(:expiryFromDate AS date) IS NULL
                OR bci.expiry_date >= CAST(:expiryFromDate AS date))

            AND (CAST(:expiryToDate AS date) IS NULL
                OR bci.expiry_date <= CAST(:expiryToDate AS date))

            AND (CAST(:componentId AS bigint) IS NULL
                OR bci.component_id = CAST(:componentId AS bigint))

            AND (CAST(:bloodGroupId AS bigint) IS NULL
                OR bci.blood_group_id = CAST(:bloodGroupId AS bigint))

            AND (CAST(:inventoryStatus AS bigint) IS NULL
                OR bci.inventory_status = CAST(:inventoryStatus AS bigint))
        """,

            nativeQuery = true
    )
    Page<BloodComponentExpiryProjection> findBloodComponentExpiryReport(
            @Param("expiryFromDate") LocalDate expiryFromDate,
            @Param("expiryToDate") LocalDate expiryToDate,
            @Param("componentId") Long componentId,
            @Param("bloodGroupId") Long bloodGroupId,
            @Param("inventoryStatus") Long inventoryStatus,
            Pageable pageable
    );

}
