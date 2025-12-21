// StoreIndentReceiveTRepository.java
package com.hims.entity.repository;

import com.hims.entity.StoreIndentReceiveT;
import com.hims.entity.StoreIssueT;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreIndentReceiveTRepository extends JpaRepository<StoreIndentReceiveT, Long> {
    List<StoreIndentReceiveT> findByStoreIndentReceiveM_ReceiveMId(Long receiveMId);
    List<StoreIndentReceiveT> findByStoreIssueT(StoreIssueT storeIssueT);
}