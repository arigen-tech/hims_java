package com.hims.entity.repository;

import com.hims.entity.MasHospital;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MasHospitalRepository extends JpaRepository<MasHospital, Long> {
    List<MasHospital> findByStatusIgnoreCase(String status);
    List<MasHospital> findByStatusInIgnoreCase(List<String> statuses);
}
