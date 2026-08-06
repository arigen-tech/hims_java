package com.hims.entity.repository;

import com.hims.entity.MasEmployee;
import com.hims.projection.MasEmployeeProjection;
import com.hims.response.DoctorResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;

@Repository
public interface MasEmployeeRepository extends JpaRepository<MasEmployee, Long> {
    List<MasEmployee> findByStatus(String status);
    Optional<MasEmployee> findByMobileNo(String mobileNo);
  //  List<MasEmployee> findByRoleIdIdAndFirstNameContainingIgnoreCaseOrderByFirstNameAsc(Long roleId, String keyword);

    List<MasEmployee> findByRoleIdIdAndStatusIgnoreCaseAndFirstNameContainingIgnoreCaseOrderByFirstNameAsc(Long roleId, String a, String keyword);

    List<MasEmployee> findByEmployeeIdInAndRoleIdIdAndStatusIgnoreCaseAndFirstNameContainingIgnoreCaseOrderByFirstNameAsc(List<Long> employeeIds, Long roleId, String a, String keyword);

    @Query(
            value = """
            SELECT 
                e.emp_id AS employeeId,
                e.emp_fn AS firstName,
                e.emp_mn AS middleName,
                e.emp_ln AS lastName,
                e.dob AS dob,
               g.id AS genderId,
                g.gender_name AS gender,

                e.mobile_no AS mobileNo,

                et.id AS employmentTypeId,
                et.employment_type AS employmentType,

                ut.user_type_id AS employeeTypeId,
                ut.user_type_name AS employeeType,

                e.status AS status,
                mr.id AS roleId,
                mr.role_desc AS roleName

            FROM mas_employee e
            LEFT JOIN mas_gender g 
                ON g.id = e.gender_id
            LEFT JOIN mas_employment_type et 
                ON et.id = e.employment_type_id
            LEFT JOIN mas_user_type ut 
                ON ut.user_type_id = e.employee_type_id
            LEFT JOIN mas_role mr
                ON e.role_id = mr.id
            WHERE 
                (
                    :employeeName IS NULL
                    OR :employeeName = ''
                    OR LOWER(COALESCE(e.emp_fn, '')) LIKE LOWER(CONCAT('%', :employeeName, '%'))
                    OR LOWER(COALESCE(e.emp_mn, '')) LIKE LOWER(CONCAT('%', :employeeName, '%'))
                    OR LOWER(COALESCE(e.emp_ln, '')) LIKE LOWER(CONCAT('%', :employeeName, '%'))
                    OR LOWER(CONCAT_WS(' ', e.emp_fn, e.emp_mn, e.emp_ln)) LIKE LOWER(CONCAT('%', :employeeName, '%'))
                )
                AND
                (
                    :mobileNo IS NULL
                    OR :mobileNo = ''
                    OR CAST(e.mobile_no AS TEXT) LIKE CONCAT('%', :mobileNo, '%')
                )
            ORDER BY e.emp_id DESC
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM mas_employee e
            WHERE 
                (
                    :employeeName IS NULL
                    OR :employeeName = ''
                    OR LOWER(COALESCE(e.emp_fn, '')) LIKE LOWER(CONCAT('%', :employeeName, '%'))
                    OR LOWER(COALESCE(e.emp_mn, '')) LIKE LOWER(CONCAT('%', :employeeName, '%'))
                    OR LOWER(COALESCE(e.emp_ln, '')) LIKE LOWER(CONCAT('%', :employeeName, '%'))
                    OR LOWER(CONCAT_WS(' ', e.emp_fn, e.emp_mn, e.emp_ln)) LIKE LOWER(CONCAT('%', :employeeName, '%'))
                )
                AND
                (
                    :mobileNo IS NULL
                    OR :mobileNo = ''
                    OR CAST(e.mobile_no AS TEXT) LIKE CONCAT('%', :mobileNo, '%')
                )
            """,
            nativeQuery = true
    )
    Page<MasEmployeeProjection> getAllEmployeesProjection(
            @Param("employeeName") String employeeName,
            @Param("mobileNo") String mobileNo,
            Pageable pageable
    );
}
