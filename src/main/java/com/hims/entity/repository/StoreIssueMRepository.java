package com.hims.entity.repository;

import com.hims.entity.StoreInternalIndentM;
import com.hims.entity.StoreIssueM;
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
}
