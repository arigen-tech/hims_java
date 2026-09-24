package com.hims.entity.repository;

import com.hims.entity.BloodCrossmatchHd;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BloodCrossmatchHdRepository extends JpaRepository<BloodCrossmatchHd, Long> {

}
