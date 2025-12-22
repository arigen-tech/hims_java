package com.hims.entity.repository;



import com.hims.entity.StoreIndentReceiveM;
import com.hims.entity.StoreInternalIndentM;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreIndentReceiveMRepository extends JpaRepository<StoreIndentReceiveM, Long> {
    StoreIndentReceiveM findByStoreInternalIndent(StoreInternalIndentM indentM);
    boolean existsByStoreInternalIndent(StoreInternalIndentM indentM);
}
