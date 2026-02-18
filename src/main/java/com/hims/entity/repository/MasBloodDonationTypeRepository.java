package com.hims.entity.repository;

import com.hims.entity.MasBloodDonationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasBloodDonationTypeRepository extends JpaRepository<MasBloodDonationType,Long> {
    List<MasBloodDonationType> findByStatusIgnoreCaseOrderByDonationTypeNameAsc(String y);

    List<MasBloodDonationType> findAllByOrderByStatusDescLastUpdateDateDesc();
}
