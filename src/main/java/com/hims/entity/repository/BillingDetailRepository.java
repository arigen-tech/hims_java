package com.hims.entity.repository;

import com.hims.entity.BillingDetail;
import com.hims.entity.BillingHeader;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BillingDetailRepository extends JpaRepository<BillingDetail, Integer> {

    List<BillingDetail> findByBillingHd(BillingHeader objHeader);

}
