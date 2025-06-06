package com.hims.entity.repository;

import com.hims.entity.MasDistrict;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MasDistrictRepository extends JpaRepository<MasDistrict, Long> {
    List<MasDistrict> findByStateIdAndStatusIgnoreCase(Long stateId, String status);
    List<MasDistrict> findByStatusIgnoreCase(String status);
    List<MasDistrict> findByStatusInIgnoreCase(List<String> statuses);
}
