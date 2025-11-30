package com.hims.entity.repository;

import com.hims.entity.StoreInternalIndentM;
import com.hims.entity.StoreInternalIndentT;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreInternalIndentTRepository extends JpaRepository<StoreInternalIndentT,Long> {

    List<StoreInternalIndentT> findByIndentM(StoreInternalIndentM indentM);
    List<StoreInternalIndentT> findByIndentM_IndentMId(Long indentMId);

    @Query(value = "SELECT DISTINCT d.item_id FROM store_internal_indent_t d " +
            "JOIN store_internal_indent_m m ON d.indent_m_id = m.indent_m_id " +
            "WHERE m.from_dept_id = :departmentId " +
            "AND m.status IN :statuses",
            nativeQuery = true)
    List<Long> findIndentedItemIds(
            @Param("departmentId") Long departmentId,
            @Param("statuses") List<String> statuses
    );
}
