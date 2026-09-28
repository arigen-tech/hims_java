package com.hims.entity.repository;

import com.hims.entity.BloodCrossmatchDt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BloodCrossmatchDtRepository
        extends JpaRepository<BloodCrossmatchDt, Long> {

}
