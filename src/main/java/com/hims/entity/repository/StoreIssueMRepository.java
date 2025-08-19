package com.hims.entity.repository;

import com.hims.entity.StoreIssueM;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreIssueMRepository extends JpaRepository<StoreIssueM,Long> {
}
