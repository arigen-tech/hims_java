package com.hims.entity.repository;

import com.hims.entity.MasSymptoms;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MasSymptomsRepository extends JpaRepository<MasSymptoms, Long> {
    // List<MasSymptoms> findByStatus(String y);

    List<MasSymptoms> findByStatusOrderBySymptomsNameAsc(String y);

    List<MasSymptoms> findAllByOrderByStatusDescLastChgDateDesc();
}
