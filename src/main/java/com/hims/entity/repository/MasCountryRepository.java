package com.hims.entity.repository;

import com.hims.entity.MasCountry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MasCountryRepository extends JpaRepository<MasCountry, Long> {
    List<MasCountry> findByStatusIgnoreCase(String status);
    List<MasCountry> findByStatusInIgnoreCase(List<String> statuses);

}
