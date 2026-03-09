
package com.hims.entity.repository;

import com.hims.entity.StoreReturnM;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreReturnMRepository extends JpaRepository<StoreReturnM, Long> {

    StoreReturnM findByStoreIndentReceiveM_StoreInternalIndent_IndentMId(Long indentMId);

    @Query("""
       SELECT r.returnMId
       FROM StoreReturnM r
       WHERE r.storeIndentReceiveM.storeInternalIndent.indentMId = :indentMId
       """)
    Long findReturnMIdByIndentMId(@Param("indentMId") Long indentMId);
}
