package com.hims.entity.repository;



import com.hims.entity.StoreIndentReceiveM;
import com.hims.entity.StoreInternalIndentM;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreIndentReceiveMRepository extends JpaRepository<StoreIndentReceiveM, Long> {
    StoreIndentReceiveM findByStoreInternalIndent(StoreInternalIndentM indentM);
    boolean existsByStoreInternalIndent(StoreInternalIndentM indentM);
    StoreIndentReceiveM findByStoreInternalIndent_IndentMId(Long indentMId);

    @Query("""
       SELECT r.receiveMId
       FROM StoreIndentReceiveM r
       WHERE r.storeInternalIndent.indentMId = :indentMId
       """)
    Long findReceiveMIdByIndentMId(@Param("indentMId") Long indentMId);
}
