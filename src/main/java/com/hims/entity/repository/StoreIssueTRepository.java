package com.hims.entity.repository;

import com.hims.entity.StoreInternalIndentT;
import com.hims.entity.StoreIssueM;
import com.hims.entity.StoreIssueT;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreIssueTRepository extends JpaRepository<StoreIssueT,Long>{


    List<StoreIssueT> findByStoreIssueMId(StoreIssueM issueM);

    List<StoreIssueT> findByIndentTId(StoreInternalIndentT indentT);

    List<StoreIssueT> findByIndentTIdAndBatchNo(StoreInternalIndentT indentT, String batchNo);
}
