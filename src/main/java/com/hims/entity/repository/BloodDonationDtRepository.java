package com.hims.entity.repository;

import com.hims.entity.BloodDonationDt;
import com.hims.entity.BloodDonationHdr;
import jdk.jfr.Registered;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BloodDonationDtRepository extends JpaRepository<BloodDonationDt,Long> {



    List<BloodDonationDt> findByDonationHdIdDonationId(BloodDonationHdr hdr);

    List<BloodDonationDt> findByDonationHdId(BloodDonationHdr hdr);
}
