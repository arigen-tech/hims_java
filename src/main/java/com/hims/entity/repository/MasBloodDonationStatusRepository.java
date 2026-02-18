package com.hims.entity.repository;

import com.hims.entity.MasBloodDonationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MasBloodDonationStatusRepository extends JpaRepository<MasBloodDonationStatus,Long> {
    List<MasBloodDonationStatus> findByStatusIgnoreCaseOrderByDonationStatusNameAsc(String y);


    List<MasBloodDonationStatus> findAllByOrderByStatusDescCreatedDate();
}
