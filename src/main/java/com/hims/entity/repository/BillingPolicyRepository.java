package com.hims.entity.repository;

import com.hims.entity.BillingPolicyMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillingPolicyRepository  extends JpaRepository<BillingPolicyMaster, Long> {




    List<BillingPolicyMaster> findAllByOrderByPolicyCodeAsc();
}
