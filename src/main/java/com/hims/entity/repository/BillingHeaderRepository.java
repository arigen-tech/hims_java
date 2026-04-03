package com.hims.entity.repository;

import com.hims.entity.BillingHeader;
import com.hims.entity.Visit;
import com.hims.projection.*;
import com.hims.response.BillingHeaderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BillingHeaderRepository extends JpaRepository<BillingHeader, Integer> {
    List<BillingHeader> findByPaymentStatusIn(List<String> paymentStatuses);

    BillingHeader findByBillNoAndPaymentStatus(String billNo, String paymentStatus);

    BillingHeader findByVisit(Visit visit);


    @Query("""
                SELECT bh
                FROM BillingHeader bh
                WHERE bh.paymentStatus IN ('y')
                  AND bh.netAmount > 0
                ORDER BY bh.createdDt DESC
            """)
    List<BillingHeader> findHeaderWithPaidDetails();


    @Query(value = """
            SELECT bh.*
            FROM billing_header bh
            INNER JOIN visit v ON bh.visit_id = v.visit_id
            WHERE LOWER(bh.payment_status) IN ('n','p')
              AND bh.net_amount > 0
              AND LOWER(v.visit_status) <> 'c'
            ORDER BY bh.created_dt DESC
            """, nativeQuery = true)
    List<BillingHeader> findPendingBilling();


    @Query(value = """
            SELECT 
                v.visit_id AS visitId,
                v.visit_date AS appointmentDate,
                bh.bill_hd_id AS billingHdId,
                p.patient_id AS patientId,
                p.uhid_no AS registrationNo,
                p.p_mobile_number AS mobileNo,
                (p.p_fn || ' ' || COALESCE(p.p_mn,'') || ' ' || p.p_ln) AS patientName,
                p.p_dob AS age,
                g.gender_name AS gender,
                r.relation_name AS relation,
                sc.service_cat_name AS billingType,
                v.doctor_name AS consultingDoctorName,
                d.department_name AS departmentName,
                bh.net_amount AS netAmount
            FROM billing_header bh
            INNER JOIN visit v ON v.billing_hd_id = bh.bill_hd_id
            INNER JOIN patient p ON v.patient_id = p.patient_id
            LEFT JOIN mas_gender g ON p.p_gender_id = g.id
            LEFT JOIN mas_relation r ON p.p_relation_id = r.relation_id
            LEFT JOIN mas_service_category sc ON bh.service_category_id = sc.id
            LEFT JOIN mas_department d ON v.department_id = d.department_id
            WHERE LOWER(bh.payment_status) IN ('n','p')
              AND bh.net_amount > 0
              AND LOWER(v.visit_status) <> 'c'
              AND bh.service_category_id = :serviceCategoryId
                          
              AND v.visit_date >= CURRENT_DATE
            
              AND (:patientName IS NULL OR 
                   LOWER(CONCAT(p.p_fn,' ',p.p_mn,' ',p.p_ln)) LIKE LOWER(CONCAT('%',:patientName,'%')))
            
              AND (:mobileNo IS NULL OR 
                   p.p_mobile_number LIKE CONCAT('%',:mobileNo,'%'))
            
              AND (:registrationNo IS NULL OR 
                   LOWER(p.uhid_no) LIKE LOWER(CONCAT('%',:registrationNo,'%')))
            
            ORDER BY bh.created_dt DESC
            """, nativeQuery = true)
    List<OpdBillingProjection> findPendingBillingByServiceCategories(
            @Param("serviceCategoryId") Long serviceCategoryId,
            @Param("patientName") String patientName,
            @Param("mobileNo") String mobileNo,
            @Param("registrationNo") String registrationNo);


    @Query(value = """
            SELECT 
            p.patient_id AS id,
            (p.p_fn || ' ' || COALESCE(p.p_mn,'') || ' ' || p.p_ln) AS fullName,
            p.p_mobile_number AS patientMobileNumber,
            EXTRACT(YEAR FROM AGE(p.p_dob)) AS patientAge,
            g.gender_name AS gender,
            
            CONCAT(
                COALESCE(p.p_address1,''), ' ',
                COALESCE(p.p_address2,''), ' ',
                COALESCE(p.p_city,''), ' ',
                COALESCE(p.p_pincode,'')
            ) AS address,
            
            r.relation_name AS relation,
            p.uhid_no AS uhidNo
            
            FROM patient p
            LEFT JOIN mas_gender g ON p.p_gender_id = g.id
            LEFT JOIN mas_relation r ON p.p_relation_id = r.relation_id
            
            WHERE p.patient_id = :patientId
            """, nativeQuery = true)
    PatientProjection getPatientDetails(@Param("patientId") Long patientId);


    @Query(value = """
            SELECT 
                v.visit_id AS visitId,
                bh.bill_hd_id AS billingHdId,
                v.visit_date AS visitDate,
                v.doctor_name AS consultedDoctor,
                d.department_name AS departmentName,
                s.session_name AS sessionName,
                v.visit_type AS visitType,
                v.token_no AS tokenNo,
            
                COALESCE(bd_reg.amount_after_discount, 0) AS registrationCost,
                COALESCE(bd_other.tariff, 0) AS tariff,
                COALESCE(bd_other.tax_percent, bd_reg.tax_percent, 0) AS taxPercent,
            
                bh.discount_amount AS discountAmount,
                bh.total_amount AS totalAmount,
                bh.tax_total AS taxAmount,
                bh.net_amount AS netAmount,
            
                bp.policy_code AS policyCode,
                bp.applicable_billing_type AS policyType,
                bp.discount_percentage AS policyDiscountPercent,
                bp.followup_days_allowed AS policyEligibilityDays,
                bp.description AS policyDescription
            
            FROM visit v
            LEFT JOIN billing_header bh 
                   ON v.visit_id = bh.visit_id
            
            -- Fetch category IDs once
            LEFT JOIN mas_service_category reg_cat 
                   ON reg_cat.service_cate_code = :regServiceCategory
            
            LEFT JOIN mas_service_category opd_cat 
                   ON opd_cat.service_cate_code = :opdServiceCategoryCode
            
            -- Registration billing
            LEFT JOIN billing_details bd_reg 
                   ON bh.bill_hd_id = bd_reg.bill_hd_id
                  AND bd_reg.service_category_id = reg_cat.id
            
            -- Other services
            LEFT JOIN billing_details bd_other 
                   ON bh.bill_hd_id = bd_other.bill_hd_id
                  AND bd_other.service_category_id <> reg_cat.id
            
            LEFT JOIN mas_department d 
                   ON v.department_id = d.department_id
            LEFT JOIN mas_opd_session s 
                   ON v.session_id = s.id
            LEFT JOIN billing_policy_master bp 
                   ON bh.billing_policy_id = bp.billing_policy_id
            
            WHERE v.patient_id = :patientId
              AND LOWER(v.visit_status) = LOWER(:visitStatus)
              AND LOWER(bh.payment_status) IN 
                  (LOWER(:paymentStatusPending), LOWER(:paymentStatusPartial))
              AND bh.service_category_id = opd_cat.id
            
            ORDER BY v.visit_date DESC
            """, nativeQuery = true)
    List<VisitBillingProjection> getVisitBillingDetails(
            @Param("patientId") Long patientId,
            @Param("opdServiceCategoryCode") String opdServiceCategoryCode,
            @Param("visitStatus") String visitStatus,
            @Param("paymentStatusPending") String paymentStatusPending,
            @Param("paymentStatusPartial") String paymentStatusPartial,
            @Param("regServiceCategory") String regServiceCategory
    );

    @Query(value = """
        SELECT
            bh.bill_hd_id AS headerId,
            v.visit_id AS visitId,
            bh.bill_no AS billNo,
            TRIM(
                COALESCE(p.p_fn, '') || ' ' ||
                COALESCE(p.p_mn, '') || ' ' ||
                COALESCE(p.p_ln, '')
            ) AS patientName,
            p.p_mobile_number AS phoneNo,
            CAST(EXTRACT(YEAR FROM AGE(p.p_dob)) AS text) AS age,
            r.relation_name AS relation,
            g.gender_name AS sex,
            d.department_name AS department,
            CAST(bh.bill_date AS text) AS billDate,
            bh.net_amount AS netAmount,
            sc.id AS serviceCategoryId,
            sc.service_cat_name AS serviceCategoryName,
            bh.payment_status AS paymentStatus,
            p.uhid_no AS registrationNo
        FROM billing_header bh
        LEFT JOIN visit v ON v.billing_hd_id = bh.bill_hd_id
        LEFT JOIN patient p ON p.patient_id = bh.patient_id
        LEFT JOIN mas_relation r ON p.p_relation_id = r.relation_id
        LEFT JOIN mas_gender g ON p.p_gender_id = g.id
        LEFT JOIN mas_department d ON v.department_id = d.department_id
        LEFT JOIN mas_service_category sc ON bh.service_category_id = sc.id
        WHERE LOWER(bh.payment_status) IN (LOWER(:complete), LOWER(:partial))
          AND (
                :patientName IS NULL OR
                LOWER(TRIM(
                    COALESCE(p.p_fn, '') || ' ' ||
                    COALESCE(p.p_mn, '') || ' ' ||
                    COALESCE(p.p_ln, '')
                )) LIKE LOWER(CONCAT('%', :patientName, '%'))
          )
          AND (
                :phoneNo IS NULL OR
                p.p_mobile_number LIKE CONCAT('%', :phoneNo, '%')
          )
          AND (
                :registrationNo IS NULL OR
                LOWER(p.uhid_no) LIKE LOWER(CONCAT('%', :registrationNo, '%'))
          )
        ORDER BY bh.bill_hd_id DESC
        """,

            countQuery = """
        SELECT COUNT(*)
        FROM billing_header bh
        LEFT JOIN visit v ON v.billing_hd_id = bh.bill_hd_id
        LEFT JOIN patient p ON p.patient_id = bh.patient_id
        WHERE LOWER(bh.payment_status) IN (LOWER(:complete), LOWER(:partial))
          AND (
                :patientName IS NULL OR
                LOWER(TRIM(
                    COALESCE(p.p_fn, '') || ' ' ||
                    COALESCE(p.p_mn, '') || ' ' ||
                    COALESCE(p.p_ln, '')
                )) LIKE LOWER(CONCAT('%', :patientName, '%'))
          )
          AND (
                :phoneNo IS NULL OR
                p.p_mobile_number LIKE CONCAT('%', :phoneNo, '%')
          )
          AND (
                :registrationNo IS NULL OR
                LOWER(p.uhid_no) LIKE LOWER(CONCAT('%', :registrationNo, '%'))
          )
        """,

            nativeQuery = true
    )
    Page<BillingHeaderResponseProjection> searchBillingStatus(
            @Param("patientName") String patientName,
            @Param("phoneNo") String phoneNo,
            @Param("registrationNo") String registrationNo,
            @Param("complete") String complete,
            @Param("partial") String partial,
            Pageable pageable
    );


    @Query(value = """
            SELECT 
                p.uhid_no AS registrationNo,
                p.p_mobile_number AS mobileNumber,
                TRIM(
                    COALESCE(p.p_fn,'') || ' ' ||
                    COALESCE(p.p_mn,'') || ' ' ||
                    COALESCE(p.p_ln,'')
                ) AS patientName,
            
                p.p_dob AS age,
                g.gender_name AS genderName,
                CAST(v.visit_date AS TEXT) AS appointmentDate,
                bh.net_amount AS netAmount,
                bh.bill_hd_id AS billingHeaderId,
                bh.patient_id AS patientId,
                bh.hdorder_id AS orderId,
                rod.appointment_date AS appointmentDateForRadiology
            FROM billing_header bh
            JOIN patient p ON p.patient_id = bh.patient_id
            LEFT JOIN mas_gender g ON g.id = p.p_gender_id
            LEFT JOIN visit v ON v.visit_id = bh.visit_id
            LEFT JOIN rad_orderhd rod ON bh.rad_order_hd_id = rod.rad_orderhd_id 
            
            WHERE LOWER(bh.payment_status) 
                  IN (LOWER(:notPaidStatus), LOWER(:partialStatus))
            AND bh.service_category_id = :serviceCategoryId
            
            AND (:patientName IS NULL OR 
                 LOWER(CONCAT(p.p_fn,' ',p.p_mn,' ',p.p_ln)) 
                 LIKE LOWER(CONCAT('%',:patientName,'%')))
            
            AND (:mobileNo IS NULL OR 
                 LOWER(p.p_mobile_number) 
                 LIKE LOWER(CONCAT('%',:mobileNo,'%')))
            
            AND (:registrationNo IS NULL OR 
                 LOWER(p.uhid_no) 
                 LIKE LOWER(CONCAT('%',:registrationNo,'%')))
            
            ORDER BY v.visit_date DESC
            """,

            countQuery = """
                    SELECT COUNT(*)
                    FROM billing_header bh
                    JOIN patient p ON p.patient_id = bh.patient_id
                    
                    WHERE LOWER(bh.payment_status) 
                          IN (LOWER(:notPaidStatus), LOWER(:partialStatus))
                    AND bh.service_category_id = :serviceCategoryId
                    
                    AND (:patientName IS NULL OR 
                         LOWER(CONCAT(p.p_fn,' ',p.p_mn,' ',p.p_ln)) 
                         LIKE LOWER(CONCAT('%',:patientName,'%')))
                    
                    AND (:mobileNo IS NULL OR 
                         LOWER(p.p_mobile_number) 
                         LIKE LOWER(CONCAT('%',:mobileNo,'%')))
                    
                    AND (:registrationNo IS NULL OR 
                         LOWER(p.uhid_no) 
                         LIKE LOWER(CONCAT('%',:registrationNo,'%')))
                    """,
            nativeQuery = true)
    <T> Page<T> findPendingBillingByCategoryId(
            @Param("serviceCategoryId") Long serviceCategoryId,
            @Param("patientName") String patientName,
            @Param("mobileNo") String mobileNo,
            @Param("registrationNo") String registrationNo,
            @Param("notPaidStatus") String notPaidStatus,
            @Param("partialStatus") String partialStatus,
            Pageable pageable,
            Class<T> type);

    @Query(value = """
            SELECT\s
                v.visit_id AS visitId,
                bh.bill_hd_id AS billinghdid,
                bh.patient_id AS patientid,
                bd.billing_dt_id AS billingdtId,
            
                p.p_fn AS firstName,
                p.p_mn AS middleName,
                p.p_ln AS lastName,
            
                p.p_mobile_number AS mobileNo,
                p.p_dob AS dob,
                p.uhid_no AS uhidNo,
                g.gender_name AS gender,
            
                r.relation_name AS relation,
                sc.service_cat_name AS billingType,
                d.department_name AS department,
            
                CONCAT_WS(', ', p.p_address1,p.p_address2, p.p_city, p.p_pincode) AS address,
            
                bh.total_amount AS billHdTotalAmount,
                bh.payment_status AS billingStatus,
            
                v.visit_date AS visitDate,
                v.token_no AS tokenNo,
            
                oh.orderhd_id AS orderhdid,
                oh.payment_status AS orderhdPaymentStatus,
            
                bd.item_name AS itemName,
                bd.quantity,
                bd.base_price AS basePrice,
                bd.tariff,
                bd.discount,
                bd.amount_after_discount AS amountAfterDiscount,
                bd.tax_percent AS taxPercent,
                bd.tax_amount AS taxAmount,
                bd.net_amount AS netAmount,
                bd.payment_status AS detailPaymentStatus,
            
                bd.investigation_id AS investigationId,
                inv.investigation_name AS investigationName,
            
                bd.package_id AS packageId,
                pkg.name AS packageName,
                            
                COALESCE(oh.appointment_date, rod.appointment_date) AS appointmentDate
            
            FROM billing_header bh
            JOIN billing_details bd ON bh.bill_hd_id = bd.bill_hd_id
            JOIN patient p ON bh.patient_id = p.patient_id
            
            LEFT JOIN mas_gender g ON p.p_gender_id = g.id
            LEFT JOIN mas_relation r ON p.p_relation_id = r.relation_id
            LEFT JOIN mas_service_category sc ON bh.service_category_id = sc.id
            LEFT JOIN visit v ON bh.visit_id = v.visit_id
            LEFT JOIN mas_department d ON v.department_id = d.department_id
            LEFT JOIN dg_orderhd oh ON bh.hdorder_id = oh.orderhd_id
            LEFT JOIN dg_mas_investigation inv ON bd.investigation_id = inv.investigation_id
            LEFT JOIN dg_investigation_package pkg ON bd.package_id = pkg.id
            LEFT JOIN rad_orderhd rod ON bh.rad_order_hd_id = rod.rad_orderhd_id
            
            WHERE bh.payment_status IN (:notPaidStatus, :partialStatus)
            AND bh.service_category_id = :categoryId
            AND bd.payment_status = :notPaidStatus
            AND bh.bill_hd_id = :billinghdId
            
            ORDER BY v.visit_date DESC
            """,
            nativeQuery = true)
    List<LabRadioBillingDetailsProjection> getUnpaidBillingDetailsByBillingHdId(
            @Param("billinghdId") Long billinghdId,
            @Param("categoryId") Long categoryId,
            @Param("notPaidStatus") String notPaidStatus,
            @Param("partialStatus") String partialStatus
    );
}
