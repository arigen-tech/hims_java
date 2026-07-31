package com.hims.entity.repository;

import com.hims.entity.StoreInternalIndentM;
import com.hims.response.IndentTrackingListResponse;
import com.hims.response.StoreInternalIndentMResponse;
import com.hims.response.StoreIssueMResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StoreInternalIndentMRepository extends JpaRepository<StoreInternalIndentM,Long> , JpaSpecificationExecutor<StoreInternalIndentM> {

    Optional<StoreInternalIndentM> findTopByOrderByIndentMIdDesc();

    // fromDeptId is MasDepartment, whose PK field name is "id"
    List<StoreInternalIndentM> findByFromDeptId_Id(Long deptId);

    // ✅ fixed: use Id (field name), not DepartmentId
    List<StoreInternalIndentM> findByFromDeptId_IdAndStatus(Long deptId, String status);

    List<StoreInternalIndentM> findByStatus(String status);


    List<StoreInternalIndentM> findByStatusOrderByIndentMIdDesc(String status);
    List<StoreInternalIndentM> findAllByOrderByIndentMIdDesc();


    List<StoreInternalIndentM> findByFromDeptId_IdAndStatusIn(Long deptId, List<String> allowedStatuses);

    // ✅ NEW: use toDeptId instead of fromDeptId
    List<StoreInternalIndentM> findByToDeptId_IdAndStatus(Long deptId, String status);

    // (optional) if you want sorted directly from DB:
    List<StoreInternalIndentM> findByToDeptId_IdAndStatusOrderByIndentMIdDesc(Long deptId, String status);


    List<StoreInternalIndentM> findByToDeptId_IdAndStatusIn(Long deptId, List<String> statuses);

    List<StoreInternalIndentM> findByFromDeptId_IdAndStatusAndIssuedDateBetween(
            Long fromDeptId,
            String status,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    Page<StoreInternalIndentM> findByFromDeptId_Id(Long deptId, Pageable pageable);

    Page<StoreInternalIndentM> findByFromDeptId_IdIn(List<Long> deptIds, Pageable pageable);

    @Query("""
    SELECT new com.hims.response.IndentTrackingListResponse(
                     :currentDeptId,
                     f.id,
                     f.departmentName,
                     t.id,
                     t.departmentName,
                     m.indentMId,
                     m.indentDate,
                     m.indentNo,
                     m.approvedDate,
                     m.issuedDate,
                     cs.commonStatusId,
                     m.status,
                     m.createdBy,
                        cs2.statusName,
                        m.isReturn
                 )
    FROM StoreInternalIndentM m
    JOIN m.fromDeptId f
    JOIN m.toDeptId t
    LEFT JOIN MasCommonStatus cs
        ON cs.entityName = 'STORE_INTERNAL_INDENT_M'
        AND cs.columnName = 'M'
        AND cs.statusCode = m.status
    LEFT JOIN MasCommonStatus cs2
        ON cs2.entityName = 'StoreInternalIndentM'
        AND cs2.columnName = 'indent_type_drug_or_nondrug'
        AND cs2.statusCode = m.indentType
    WHERE (:deptIds IS NULL OR f.id IN :deptIds)
""")
    Page<IndentTrackingListResponse>
    findIndentTrackingListForAdmin(
            @Param("deptIds") List<Long> deptIds,
            @Param("currentDeptId") Long currentDeptId,
            Pageable pageable
    );


    @Query(
            value = """
    SELECT new com.hims.response.IndentTrackingListResponse(
                     :currentDeptId,
                     f.id,
                     f.departmentName,
                     t.id,
                     t.departmentName,
                     m.indentMId,
                     m.indentDate,
                     m.indentNo,
                     m.approvedDate,
                     m.issuedDate,
                     cs.commonStatusId,
                     m.status,
                     m.createdBy,
                     cs2.statusName,
                     m.isReturn
                 )
    FROM StoreInternalIndentM m
    JOIN m.fromDeptId f
    JOIN m.toDeptId t
    LEFT JOIN MasCommonStatus cs
        ON cs.tableName = 'store_internal_indent_m'
        AND cs.columnName = 'status'
        AND cs.statusCode = m.status
    LEFT JOIN MasCommonStatus cs2
        ON cs2.tableName = 'store_internal_indent_m'
        AND cs2.columnName = 'indent_type_drug_or_nondrug'
        AND cs2.statusCode = m.indentType
    WHERE f.id = :fromDeptId
        AND m.indentDate >= COALESCE(:fromDate, m.indentDate)
        AND m.indentDate <= COALESCE(:toDate, m.indentDate)
    """,
            countQuery = """
    SELECT COUNT(m)
    FROM StoreInternalIndentM m
    JOIN m.fromDeptId f
    JOIN m.toDeptId t
    WHERE f.id = :fromDeptId
        AND m.indentDate >= COALESCE(:fromDate, m.indentDate)
        AND m.indentDate <= COALESCE(:toDate, m.indentDate)
    """
    )
    Page<IndentTrackingListResponse> searchIndentTrackingListProjection(
            @Param("fromDeptId") Long fromDeptId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("currentDeptId") Long currentDeptId,
            Pageable pageable
    );


    @Query("""
SELECT new com.hims.response.IndentTrackingListResponse(
    m.fromDeptId.id,
    m.fromDeptId.id,
    m.fromDeptId.departmentName,
    m.toDeptId.id,
    m.toDeptId.departmentName,
    m.indentMId,
    m.indentDate,
    m.indentNo,
    m.approvedDate,
    m.issuedDate,
    cs.commonStatusId,
    cs.statusName,
    m.createdBy,
    cs2.statusName,
    m.isReturn
)
FROM StoreInternalIndentM m
JOIN MasCommonStatus cs
     ON cs.statusCode = m.status
     AND cs.tableName = 'store_internal_indent_m'
JOIN MasCommonStatus cs2
     ON cs2.statusCode = m.indentType
     AND cs2.tableName = 'store_internal_indent_m'
WHERE m.fromDeptId.id = :deptId
            AND m.indentDate >= COALESCE(:fromDate, m.indentDate)
                                         AND m.indentDate <= COALESCE(:toDate, m.indentDate)
AND (m.status IN :statuses)
ORDER BY m.indentDate DESC
""")
    Page<IndentTrackingListResponse> findIndentListForViewUpdate(
            @Param("deptId") Long deptId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("statuses") List<String> statuses,
            Pageable pageable
    );

    @Query("""
SELECT new com.hims.response.IndentTrackingListResponse(
    f.id,
    f.id,
    f.departmentName,
    t.id,
    t.departmentName,
    m.indentMId,
    m.indentDate,
    m.indentNo,
    m.approvedDate,
    m.issuedDate,
    cs.commonStatusId,
    cs.statusName,
    m.createdBy,
    cs2.statusName,
    m.isReturn
)
FROM StoreInternalIndentM m
LEFT JOIN m.fromDeptId f
LEFT JOIN m.toDeptId t
LEFT JOIN MasCommonStatus cs
       ON cs.statusCode = m.status
       AND cs.tableName = 'store_internal_indent_m'
       AND cs.columnName = 'status'
       LEFT JOIN MasCommonStatus cs2
       ON cs2.statusCode = m.indentType
       AND cs2.tableName = 'store_internal_indent_m'
       AND cs2.columnName = 'indent_type_drug_or_nondrug'
WHERE f.id = :deptId
AND m.status = :status
ORDER BY m.indentMId DESC
""")
    List<IndentTrackingListResponse> pendingForIndentApprovalWrtDept(
            @Param("deptId") Long deptId,@Param("status") String status
    );

    @Query("""
SELECT new com.hims.response.IndentTrackingListResponse(
    :deptId,
    f.id,
    f.departmentName,
    t.id,
    t.departmentName,
    m.indentMId,
    m.indentDate,
    m.indentNo,
    m.approvedDate,
    m.issuedDate,
    cs.commonStatusId,
    cs.statusName,
    m.createdBy,
    cs2.statusName,
    m.isReturn
)
FROM StoreInternalIndentM m
LEFT JOIN m.fromDeptId f
LEFT JOIN m.toDeptId t
LEFT JOIN MasCommonStatus cs
       ON cs.statusCode = m.status
       AND cs.tableName = 'store_internal_indent_m'
       AND cs.columnName = 'status'
LEFT JOIN MasCommonStatus cs2
       ON cs2.statusCode = m.indentType
       AND cs2.tableName = 'store_internal_indent_m'
       AND cs2.columnName = 'indent_type_drug_or_nondrug'
WHERE t.id = :deptId
AND m.status = :status
ORDER BY m.indentMId DESC
""")
    List<IndentTrackingListResponse> findIndentsWrtStatus(
            @Param("deptId") Long deptId,@Param("status") String status
    );


    @Query("""
SELECT new com.hims.response.StoreInternalIndentMResponse(
    m.indentMId,
    m.indentNo,
    m.indentDate,
    f.id,
    f.departmentName,
    t.id,
    t.departmentName,
    cs.statusName,
    m.createdBy,
    m.approvedBy,
    m.approvedDate,
    m.storeApprovedBy,
    m.storeApprovedDate,
    m.issuedBy,
    m.issuedDate,
    m.receivedBy,
    m.receivedDate,
    m.remarks,
    cs2.statusName
)
FROM StoreInternalIndentM m
LEFT JOIN m.fromDeptId f
LEFT JOIN m.toDeptId t
LEFT JOIN MasCommonStatus cs
       ON cs.statusCode = m.status
       AND cs.tableName = 'store_internal_indent_m'
       AND cs.columnName = 'status'
LEFT JOIN MasCommonStatus cs2
       ON cs2.statusCode = m.indentType
       AND cs2.tableName = 'store_internal_indent_m'
       AND cs2.columnName = 'indent_type_drug_or_nondrug'
WHERE t.id = :deptId
AND m.status = :status
ORDER BY m.indentMId DESC
""")
    List<StoreInternalIndentMResponse> findIndentsWrtToDeptAndStatus(
            @Param("deptId") Long deptId,@Param("status") String status
    );

    @Query("""
SELECT new com.hims.response.StoreIssueMResponse(
    sim.storeIssueMId,
    sim.issueNo,
    sim.issueDate,
    im.indentMId,
    im.indentNo,
    im.indentDate
)
FROM StoreInternalIndentM im
LEFT JOIN im.storeIssueMId sim
WHERE im.status = 'FI'
AND im.status != 'RC'
AND (:fromDeptId IS NULL OR im.fromDeptId.id = :fromDeptId)
AND im.issuedDate BETWEEN
        COALESCE(:fromDate, im.issuedDate)
    AND COALESCE(:toDate, im.issuedDate)
ORDER BY im.indentDate DESC
""")
    List<StoreIssueMResponse> findIndentMForReceiving(
            Long fromDeptId,
            LocalDateTime fromDate,
            LocalDateTime toDate
    );

}
