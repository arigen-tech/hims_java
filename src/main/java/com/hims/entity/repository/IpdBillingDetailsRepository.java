package com.hims.entity.repository;

import com.hims.entity.IpdBillingDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IpdBillingDetailsRepository extends JpaRepository<IpdBillingDetails,Long> {
}
