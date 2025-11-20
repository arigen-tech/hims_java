package com.hims.entity.repository;

import com.hims.entity.StoreInternalIndentM;
import com.hims.entity.StoreInternalIndentT;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreInternalIndentTRepository extends JpaRepository<StoreInternalIndentT,Long> {

    List<StoreInternalIndentT> findByIndentM(StoreInternalIndentM indentM);
    List<StoreInternalIndentT> findByIndentM_IndentMId(Long indentMId);

}
