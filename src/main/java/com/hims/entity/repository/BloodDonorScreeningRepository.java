package com.hims.entity.repository;

import com.hims.entity.BloodDonorScreening;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BloodDonorScreeningRepository extends JpaRepository<BloodDonorScreening,Long> {

}
