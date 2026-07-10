package com.hims.entity.repository;

import com.hims.entity.StoreBalanceHd;
import com.hims.response.OpeningBalanceEntryHeaderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StoreBalanceHdRepository extends JpaRepository<StoreBalanceHd,Long> {
    List<StoreBalanceHd> findByStatus(String status);

    List<StoreBalanceHd> findByStatusIn(List<String> statusList);
    Optional<StoreBalanceHd> findByBalanceMIdAndHospitalIdIdAndDepartmentIdId(Long id, Long hospitalId, Long departmentId);

    List<StoreBalanceHd> findByStatusInAndHospitalIdIdAndDepartmentIdId(List<String> list, Long hospitalId, Long departmentId);

    @Query(
            value = """
SELECT new com.hims.response.OpeningBalanceEntryHeaderResponse(
    sb.balanceMId,
    sb.balanceNo,
    sb.hospitalId.id,
    sb.departmentId.id,
    sb.departmentId.departmentName,
    sb.enteredBy,
    sb.remarks,
    sb.status,
    sb.enteredDt,
    sb.approvedBy,
    sb.approvalDt,
    sb.lastUpdatedDt,
    mcs.statusName
)
FROM StoreBalanceHd sb
LEFT JOIN MasCommonStatus mcs
       ON mcs.statusCode = sb.balanceType
       AND mcs.entityName = 'StoreBalanceHd'
       AND mcs.columnName = 'balance_type_drug_or_nondrug'
WHERE sb.hospitalId.id = :hospitalId
AND sb.departmentId.id = :departmentId
AND LOWER(sb.status) IN :statuses
AND sb.enteredDt >= COALESCE(:fromDate, sb.enteredDt)
AND sb.enteredDt <= COALESCE(:toDate, sb.enteredDt)
""",
            countQuery = """
SELECT COUNT(sb)
FROM StoreBalanceHd sb
WHERE sb.hospitalId.id = :hospitalId
AND sb.departmentId.id = :departmentId
AND LOWER(sb.status) IN :statuses
AND sb.enteredDt >= COALESCE(:fromDate, sb.enteredDt)
AND sb.enteredDt <= COALESCE(:toDate, sb.enteredDt)
"""
    )
    Page<OpeningBalanceEntryHeaderResponse> findOpeningBalanceHeadersWrtDept(
            @Param("hospitalId") Long hospitalId,
            @Param("departmentId") Long departmentId,
            @Param("statuses") List<String> statuses,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable
    );

    @Query(
            value = """
SELECT new com.hims.response.OpeningBalanceEntryHeaderResponse(
    sb.balanceMId,
    sb.balanceNo,
    sb.hospitalId.id,
    sb.departmentId.id,
    sb.departmentId.departmentName,
    sb.enteredBy,
    sb.remarks,
    sb.status,
    sb.enteredDt,
    sb.approvedBy,
    sb.approvalDt,
    sb.lastUpdatedDt,
    mcs.statusName
)
FROM StoreBalanceHd sb
LEFT JOIN MasCommonStatus mcs
       ON mcs.statusCode = sb.balanceType
       AND mcs.entityName = 'StoreBalanceHd'
       AND mcs.columnName = 'balance_type_drug_or_nondrug'
WHERE sb.hospitalId.id = :hospitalId
AND sb.departmentId.id = :departmentId
AND LOWER(sb.status) = :status
"""
    )
    List<OpeningBalanceEntryHeaderResponse> findOpeningBalanceHeadersWrtDeptWithoutPagination(
            @Param("hospitalId") Long hospitalId,
            @Param("departmentId") Long departmentId,
            @Param("status") String status
    );
}
