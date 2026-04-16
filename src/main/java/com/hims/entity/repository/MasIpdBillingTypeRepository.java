package com.hims.entity.repository;

import com.hims.entity.MasIpdBillingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasIpdBillingTypeRepository extends JpaRepository<MasIpdBillingType,Long> {
    List<MasIpdBillingType> findByStatusIgnoreCaseOrderByBillingTypeNameAsc(String lowerCase);

    List<MasIpdBillingType> findAllByOrderByStatusDescLastUpdateDateDesc();
}
