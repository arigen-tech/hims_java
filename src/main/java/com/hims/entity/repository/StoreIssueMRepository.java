package com.hims.entity.repository;

import com.hims.entity.StoreInternalIndentM;
import com.hims.entity.StoreIssueM;
import com.hims.response.StoreIssueMResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StoreIssueMRepository extends JpaRepository<StoreIssueM,Long> {
    List<StoreIssueM> findByIndentMId(StoreInternalIndentM indent);


    @Query("""
    SELECT sim FROM StoreIssueM sim
    WHERE sim.fromStoreId.id = :fromDeptId
    AND sim.issueDate BETWEEN :fromDate AND :toDate
    ORDER BY sim.issueDate DESC
""")
    List<StoreIssueM> findIssuesBetweenDates(
            @Param("fromDeptId") Long fromDeptId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );

    @Query("""
    SELECT new com.hims.response.StoreIssueMResponse(
        sim.storeIssueMId,
        sim.issueNo,
        sim.issueDate,
        sim.indentMId.indentMId,
        sim.indentMId.indentNo,
        sim.indentMId.indentDate
    )
    FROM StoreIssueM sim
    WHERE sim.toDeptId.id = :toDeptId
    AND sim.issueDate BETWEEN :fromDate AND :toDate
    ORDER BY sim.issueDate DESC
""")
    List<StoreIssueMResponse> findIssuesBetweenDatesWrtToDept(
            @Param("toDeptId") Long toDeptId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate
    );

    StoreIssueM findByIndentMId_IndentMId(Long indentMId);

    @Query("""
       SELECT s.storeIssueMId
       FROM StoreIssueM s
       WHERE s.indentMId.indentMId = :indentMId
       """)
    Long findIssueMIdByIndentMId(@Param("indentMId") Long indentMId);
}
