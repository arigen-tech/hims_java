package com.hims.entity.repository;

import com.hims.entity.PaymentRefund;
import com.hims.projection.PaidCancelledAppointmentProjection;
import com.hims.projection.RefundDetailsProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PaymentRefundRepository extends JpaRepository<PaymentRefund, Long> {

    Optional<PaymentRefund> findByGatewayRefundId(String gatewayRefundId);

    @Query("""
    SELECT COALESCE(SUM(r.refundAmount), 0)
    FROM PaymentRefund r
    WHERE r.payment.paymentId = :paymentId
      AND r.refundStatusId IN :statusIds
""")
    BigDecimal getTotalRefundedAmount(
            @Param("paymentId") Long paymentId,
            @Param("statusIds") List<Long> statusIds
    );

    Optional<PaymentRefund> findByRefundReferenceNo(String refundReferenceNo);

    List<PaymentRefund> findByPayment_PaymentId(Long paymentId);

    @Query("""
    SELECT
        pr.refundId AS refundId,
        pr.gatewayRefundId AS gatewayRefundId,
        pr.refundReferenceNo AS refundNumber,
        pr.refundAmount AS refundAmount,
        p.amount AS paymentAmount,
        mps.statusName AS refundStatus,
        acr.reasonName AS refundReason,
        pr.refundRequestedAt AS initiatedOn,
        p.gatewayPaymentId AS gatewayPaymentId,
        mpm.modeName AS paymentMode,
        p.paymentVia AS paymentVia,
        pr.gatewayReferenceType AS gatewayReferenceType,
        pr.gatewayReferenceNo AS gatewayReferenceNo                     
    FROM PaymentRefund pr
    JOIN pr.payment p
    LEFT JOIN MasPaymentMode  mpm on mpm.paymentModeId =p.paymentModeId
    LEFT JOIN MasPaymentStatus mps on mps.id =pr.refundStatusId
    LEFT JOIN pr.appointmentChangeReason acr
    WHERE pr.refundId = :refundId
    """)
    Optional<RefundDetailsProjection> findRefundDetailsById(
            @Param("refundId") Long refundId
    );

//    @Query(
//            value = """
//        SELECT
//            v.visit_id AS visitId,
//            bh.patient_id AS patientId,
//            bh.bill_hd_id AS billingHeaderId,
//
//            TRIM(
//                CONCAT(
//                    COALESCE(p.p_fn, ''), ' ',
//                    COALESCE(p.p_mn, ''), ' ',
//                    COALESCE(p.p_ln, '')
//                )
//            ) AS patientName,
//
//            p.p_mobile_number AS mobileNo,
//            p.p_age AS age,
//            g.gender_name AS gender,
//
//            dt.department_type_name AS billingType,
//
//            v.visit_date AS date,
//            bh.bill_date AS billDate,
//            v.cancelled_datetime AS cancelledDate,
//
//            CAST(COALESCE(bh.net_amount, 0) AS BIGINT) AS billingAmount,
//
//            pd.payment_id AS paymentId,
//
//            pr.refund_id AS refundId,
//            pr.refund_amount AS refundAmount,
//            pr.refund_reference_no AS refundReferenceNo,
//            pr.refund_reason AS refundReason,
//            pr.gateway_refund_id AS gatewayRefundId,
//
//            pr.refund_processed_at AS refundDate,
//
//            COALESCE(
//                mps.payment_status_name,
//                'PENDING'
//            ) AS refundStatus,
//
//            mpg.payment_gateway_id AS paymentModeId,
//            mpg.gateway_code AS paymentModeCode,
//            mpg.gateway_name AS paymentModeName,
//
//            d.department_name AS departmentName
//
//        FROM payment_refund pr
//
//        INNER JOIN payment_details_v2 pd
//            ON pd.payment_id = pr.payment_id
//
//        INNER JOIN billing_header bh
//            ON bh.bill_hd_id = pd.billing_hd_id
//
//        INNER JOIN patient p
//            ON p.patient_id = bh.patient_id
//
//        INNER JOIN visit v
//            ON v.visit_id = bh.visit_id
//
//        LEFT JOIN mas_gender g
//            ON g.id = p.p_gender_id
//
//        LEFT JOIN mas_department d
//            ON d.department_id = v.department_id
//
//        LEFT JOIN mas_department_type dt
//            ON dt.department_type_id = d.department_type_id
//
//        LEFT JOIN mas_payment_gateway mpg
//            ON mpg.payment_gateway_id = pr.payment_gateway_id
//
//        LEFT JOIN mas_payment_status mps
//            ON mps.payment_status_id = pr.refund_status_id
//
//        WHERE LOWER(v.visit_status) = 'c'
//          AND LOWER(v.billing_status) = 'y'
//          AND COALESCE(bh.net_amount, 0) > 0
//
//          AND (
//              :patientName IS NULL
//              OR :patientName = ''
//              OR LOWER(
//                  CONCAT(
//                      COALESCE(p.p_fn, ''), ' ',
//                      COALESCE(p.p_mn, ''), ' ',
//                      COALESCE(p.p_ln, '')
//                  )
//              ) LIKE LOWER(CONCAT('%', :patientName, '%'))
//          )
//
//          AND (
//              :mobileNo IS NULL
//              OR :mobileNo = ''
//              OR p.p_mobile_number LIKE CONCAT('%', :mobileNo, '%')
//          )
//
//          AND (
//              :billingService IS NULL
//              OR :billingService = ''
//              OR LOWER(
//                  COALESCE(dt.department_type_code, '')
//              ) LIKE LOWER(CONCAT('%', :billingService, '%'))
//          )
//
//          AND (
//              :refundStatus IS NULL
//              OR :refundStatus = ''
//              OR mps.payment_status_code = :refundStatus
//          )
//
//          AND (
//              :paymentModeId IS NULL
//              OR pr.payment_gateway_id = :paymentModeId
//          )
//
//        ORDER BY pr.refund_requested_at DESC
//        """,
//
//            countQuery = """
//        SELECT COUNT(pr.refund_id)
//
//        FROM payment_refund pr
//
//        INNER JOIN payment_details_v2 pd
//            ON pd.payment_id = pr.payment_id
//
//        INNER JOIN billing_header bh
//            ON bh.bill_hd_id = pd.billing_hd_id
//
//        INNER JOIN patient p
//            ON p.patient_id = bh.patient_id
//
//        INNER JOIN visit v
//            ON v.visit_id = bh.visit_id
//
//        LEFT JOIN mas_department d
//            ON d.department_id = v.department_id
//
//        LEFT JOIN mas_department_type dt
//            ON dt.department_type_id = d.department_type_id
//
//        LEFT JOIN mas_payment_status mps
//            ON mps.payment_status_id = pr.refund_status_id
//
//        WHERE LOWER(v.visit_status) = 'c'
//          AND LOWER(v.billing_status) = 'y'
//          AND COALESCE(bh.net_amount, 0) > 0
//
//          AND (
//              :patientName IS NULL
//              OR :patientName = ''
//              OR LOWER(
//                  CONCAT(
//                      COALESCE(p.p_fn, ''), ' ',
//                      COALESCE(p.p_mn, ''), ' ',
//                      COALESCE(p.p_ln, '')
//                  )
//              ) LIKE LOWER(CONCAT('%', :patientName, '%'))
//          )
//
//          AND (
//              :mobileNo IS NULL
//              OR :mobileNo = ''
//              OR p.p_mobile_number LIKE CONCAT('%', :mobileNo, '%')
//          )
//
//          AND (
//              :billingService IS NULL
//              OR :billingService = ''
//              OR LOWER(
//                  COALESCE(dt.department_type_code, '')
//              ) LIKE LOWER(CONCAT('%', :billingService, '%'))
//          )
//
//          AND (
//              :refundStatus IS NULL
//              OR :refundStatus = ''
//              OR mps.payment_status_code = :refundStatus
//          )
//
//          AND (
//              :paymentModeId IS NULL
//              OR pr.payment_gateway_id = :paymentModeId
//          )
//        """,
//
//            nativeQuery = true
//    )


//    Page<PaidCancelledAppointmentProjection> getBillingRefundPatientList(
//            @Param("patientName") String patientName,
//            @Param("mobileNo") String mobileNo,
//            @Param("billingService") String billingService,
//            @Param("refundStatus") String refundStatus,
//            @Param("paymentModeId") Long paymentModeId,
//            Pageable pageable
//    );

    @Query(
            value = """
        SELECT
            v.visit_id AS visitId,
            bh.patient_id AS patientId,
            bh.bill_hd_id AS billingHeaderId,

            TRIM(
                CONCAT(
                    COALESCE(p.p_fn, ''), ' ',
                    COALESCE(p.p_mn, ''), ' ',
                    COALESCE(p.p_ln, '')
                )
            ) AS patientName,

            p.p_mobile_number AS mobileNo,
            p.p_mobile_number AS mobileNumber,
            p.p_age AS age,
            g.gender_name AS gender,

            v.doctor_id AS doctorId,
            v.doctor_name AS doctorName,
            v.department_id AS departmentId,
            DATE(v.visit_date) AS appointmentDate,
            CONCAT(
                TO_CHAR(v.start_time, 'HH24:MI'),
                ' to ',
                TO_CHAR(v.end_time, 'HH24:MI')
            ) AS appointmentTime,
            v.cancelled_datetime AS cancellationDateTime,
            v.cancelled_by AS cancelledBy,
            r.reason_name AS cancellationReason,

            dt.department_type_name AS billingType,

            v.visit_date AS date,
            bh.bill_date AS billDate,
            v.cancelled_datetime AS cancelledDate,

            CAST(COALESCE(bh.net_amount, 0) AS BIGINT) AS billingAmount,

            pd.payment_id AS paymentId,

            pr.refund_id AS refundId,
            pr.refund_amount AS refundAmount,
            pr.refund_reference_no AS refundReferenceNo,
            pr.refund_reason AS refundReason,
            pr.gateway_refund_id AS gatewayRefundId,

            pr.refund_processed_at AS refundDate,

            CASE
            WHEN mps.payment_status_code = 'REFUNDED' THEN
                CASE
                    WHEN pr.gateway_reference_type IS NULL
                         OR pr.gateway_reference_no IS NULL
                    THEN 'PROCESSED'
                    ELSE 'REFUNDED'
                END
            ELSE 'PENDING'
        END AS refundStatus,

            mpg.payment_gateway_id AS paymentModeId,
            mpg.gateway_code AS paymentModeCode,
            mpg.gateway_name AS paymentModeName,

            d.department_name AS departmentName

        FROM payment_refund pr

        INNER JOIN payment_details_v2 pd
            ON pd.payment_id = pr.payment_id

        INNER JOIN billing_header bh
            ON bh.bill_hd_id = pd.billing_hd_id

        INNER JOIN patient p
            ON p.patient_id = bh.patient_id

        INNER JOIN visit v
            ON v.visit_id = bh.visit_id

        LEFT JOIN mas_gender g
            ON g.id = p.p_gender_id

        LEFT JOIN mas_department d
            ON d.department_id = v.department_id

        LEFT JOIN mas_department_type dt
            ON dt.department_type_id = d.department_type_id

        LEFT JOIN mas_payment_gateway mpg
            ON mpg.payment_gateway_id = pr.payment_gateway_id

        LEFT JOIN mas_payment_status mps
            ON mps.payment_status_id = pr.refund_status_id

        LEFT JOIN mas_appointment_change_reason r
            ON r.reason_id = v.cancelled_reason_id

        WHERE LOWER(v.visit_status) = 'c'
          AND LOWER(v.billing_status) = 'y'
          AND COALESCE(bh.net_amount, 0) > 0

          AND (
              :patientName IS NULL
              OR :patientName = ''
              OR LOWER(
                  CONCAT(
                      COALESCE(p.p_fn, ''), ' ',
                      COALESCE(p.p_mn, ''), ' ',
                      COALESCE(p.p_ln, '')
                  )
              ) LIKE LOWER(CONCAT('%', :patientName, '%'))
          )

          AND (
              :mobileNo IS NULL
              OR :mobileNo = ''
              OR p.p_mobile_number LIKE CONCAT('%', :mobileNo, '%')
          )

          AND (
              :billingService IS NULL
              OR :billingService = ''
              OR LOWER(
                  COALESCE(dt.department_type_code, '')
              ) LIKE LOWER(CONCAT('%', :billingService, '%'))
          )

          AND (
          :refundStatus IS NULL
          OR :refundStatus = ''
          OR (
              CASE
                  WHEN mps.payment_status_code = 'REFUNDED' THEN
                      CASE
                          WHEN pr.gateway_reference_type IS NULL
                               OR pr.gateway_reference_no IS NULL
                          THEN 'PROCESSED'
                          ELSE 'REFUNDED'
                      END
                  ELSE 'PENDING'
              END
          ) = :refundStatus
      )

          AND (
              :paymentModeId IS NULL
              OR pr.refund_mode_id = :paymentModeId
          )

        ORDER BY pr.refund_requested_at DESC
        """,

            countQuery = """
    SELECT COUNT(pr.refund_id)

    FROM payment_refund pr

    INNER JOIN payment_details_v2 pd
        ON pd.payment_id = pr.payment_id

    INNER JOIN billing_header bh
        ON bh.bill_hd_id = pd.billing_hd_id

    INNER JOIN patient p
        ON p.patient_id = bh.patient_id

    INNER JOIN visit v
        ON v.visit_id = bh.visit_id

    LEFT JOIN mas_department d
        ON d.department_id = v.department_id

    LEFT JOIN mas_department_type dt
        ON dt.department_type_id = d.department_type_id

    LEFT JOIN mas_payment_status mps
        ON mps.payment_status_id = pr.refund_status_id

    WHERE LOWER(v.visit_status) = 'c'
      AND LOWER(v.billing_status) = 'y'
      AND COALESCE(bh.net_amount, 0) > 0

      AND (
          :patientName IS NULL
          OR :patientName = ''
          OR LOWER(
              CONCAT(
                  COALESCE(p.p_fn, ''), ' ',
                  COALESCE(p.p_mn, ''), ' ',
                  COALESCE(p.p_ln, '')
              )
          ) LIKE LOWER(CONCAT('%', :patientName, '%'))
      )

      AND (
          :mobileNo IS NULL
          OR :mobileNo = ''
          OR p.p_mobile_number LIKE CONCAT('%', :mobileNo, '%')
      )

      AND (
          :billingService IS NULL
          OR :billingService = ''
          OR LOWER(
              COALESCE(dt.department_type_code, '')
          ) LIKE LOWER(CONCAT('%', :billingService, '%'))
      )

      AND (
          :refundStatus IS NULL
          OR :refundStatus = ''
          OR (
              CASE
                  WHEN mps.payment_status_code = 'REFUNDED' THEN
                      CASE
                          WHEN pr.gateway_reference_type IS NULL
                               OR pr.gateway_reference_no IS NULL
                          THEN 'PROCESSED'
                          ELSE 'REFUNDED'
                      END
                  ELSE 'PENDING'
              END
          ) = :refundStatus
      )

      AND (
          :paymentModeId IS NULL
          OR pr.refund_mode_id = :paymentModeId
      )
    """,

            nativeQuery = true
    )
    Page<PaidCancelledAppointmentProjection> getBillingRefundPatientList(
            @Param("patientName") String patientName,
            @Param("mobileNo") String mobileNo,
            @Param("billingService") String billingService,
            @Param("refundStatus") String refundStatus,
            @Param("paymentModeId") Long paymentModeId,
            Pageable pageable
    );

    @Query(value = """
        SELECT
            v.visit_id AS visitId,
            v.patient_id AS patientId,
            bh.bill_hd_id AS billingHeaderId,
            TRIM(CONCAT(
                COALESCE(p.p_fn, ''), ' ',
                COALESCE(p.p_mn, ''), ' ',
                COALESCE(p.p_ln, '')
            )) AS patientName,
            p.p_mobile_number AS mobileNo,
            p.p_mobile_number AS mobileNumber,
            p.p_age AS age,
            g.gender_name AS gender,
            v.doctor_id AS doctorId,
            v.doctor_name AS doctorName,
            v.department_id AS departmentId,
            DATE(v.visit_date) AS appointmentDate,
            CONCAT(
                TO_CHAR(v.start_time, 'HH24:MI'),
                ' to ',
                TO_CHAR(v.end_time, 'HH24:MI')
            ) AS appointmentTime,
            v.cancelled_datetime AS cancellationDateTime,
            v.cancelled_by AS cancelledBy,
            r.reason_name AS cancellationReason,
            dt.department_type_name AS billingType,
            v.visit_date AS date,
            bh.bill_date AS billDate,
            v.cancelled_datetime AS cancelledDate,
            CAST(COALESCE(bh.net_amount, 0) AS BIGINT) AS billingAmount,
            pd.payment_id AS paymentId,
            pr.refund_id AS refundId,
            pr.refund_amount AS refundAmount,
            pr.refund_reference_no AS refundReferenceNo,
            pr.refund_reason AS refundReason,
            pr.gateway_refund_id AS gatewayRefundId,
            pr.refund_processed_at AS refundDate,
            CASE
                WHEN mps.payment_status_code = 'REFUNDED' THEN
                    CASE
                        WHEN pr.gateway_reference_type IS NULL
                             OR pr.gateway_reference_no IS NULL
                        THEN 'PROCESSED'
                        ELSE 'REFUNDED'
                    END
                ELSE 'PENDING'
            END AS refundStatus,
            mpg.payment_gateway_id AS paymentModeId,
            mpg.gateway_code AS paymentModeCode,
            mpg.gateway_name AS paymentModeName,
            d.department_name AS departmentName
        FROM visit v
        INNER JOIN patient p ON p.patient_id = v.patient_id
        LEFT JOIN billing_header bh ON bh.visit_id = v.visit_id
        LEFT JOIN payment_details_v2 pd ON pd.billing_hd_id = bh.bill_hd_id
        LEFT JOIN payment_refund pr ON pr.payment_id = pd.payment_id
        LEFT JOIN mas_gender g ON g.id = p.p_gender_id
        LEFT JOIN mas_department d ON d.department_id = v.department_id
        LEFT JOIN mas_department_type dt ON dt.department_type_id = d.department_type_id
        LEFT JOIN mas_appointment_change_reason r ON r.reason_id = v.cancelled_reason_id
        LEFT JOIN mas_payment_gateway mpg ON mpg.payment_gateway_id = pr.payment_gateway_id
        LEFT JOIN mas_payment_status mps ON mps.payment_status_id = pr.refund_status_id
        WHERE v.hospital_id = :hospitalId
          AND v.patient_id = :patientId
          AND LOWER(v.visit_status) = 'c'
          AND LOWER(dt.department_type_code) = LOWER(:departmentType)
        ORDER BY v.cancelled_datetime DESC, pr.refund_id DESC
        """,
        countQuery = """
                SELECT COUNT(DISTINCT v.visit_id)
                FROM visit v
                INNER JOIN patient p ON p.patient_id = v.patient_id
                LEFT JOIN billing_header bh ON bh.visit_id = v.visit_id
                LEFT JOIN payment_details_v2 pd ON pd.billing_hd_id = bh.bill_hd_id
                LEFT JOIN payment_refund pr ON pr.payment_id = pd.payment_id
        INNER JOIN mas_department d ON d.department_id = v.department_id
        INNER JOIN mas_department_type dt ON dt.department_type_id = d.department_type_id
        WHERE v.hospital_id = :hospitalId
          AND v.patient_id = :patientId
          AND LOWER(v.visit_status) = 'c'
          AND LOWER(dt.department_type_code) = LOWER(:departmentType)
        """,
        nativeQuery = true)
    Page<PaidCancelledAppointmentProjection> findMobileCancelledRefundAppointments(
            @Param("hospitalId") Long hospitalId,
            @Param("departmentType") String departmentType,
            @Param("patientId") Long patientId,
            Pageable pageable
    );

    List<PaymentRefund> findByPaymentGatewayIdAndGatewayReferenceNoIsNullAndGatewayReferenceTypeIsNull(Long paymentGatewayId);

}