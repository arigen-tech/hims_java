package com.hims.entity.repository;

import com.hims.entity.MasInsurance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasInsuranceRepository extends JpaRepository<MasInsurance,Long> {
    List<MasInsurance> findByStatusIgnoreCaseOrderByInsuranceNameAsc(String lowerCase);

    List<MasInsurance> findAllByOrderByStatusDescLastChgDateDesc();
}
