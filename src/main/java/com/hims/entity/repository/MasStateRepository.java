package com.hims.entity.repository;

import com.hims.entity.MasState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MasStateRepository extends JpaRepository<MasState, Long> {
    List<MasState> findByCountryIdAndStatusIgnoreCase(Long countryId, String status);
    List<MasState> findByStatusIgnoreCase(String status);
    List<MasState> findByStatusInIgnoreCase(List<String> statuses);
}
