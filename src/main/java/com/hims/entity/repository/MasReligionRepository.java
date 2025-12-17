package com.hims.entity.repository;

import com.hims.entity.MasReligion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MasReligionRepository extends JpaRepository<MasReligion, Long> {
    List<MasReligion> findByStatusIgnoreCase(String status);
    List<MasReligion> findByStatusInIgnoreCase(List<String> statuses);

    List<MasReligion> findByStatusIgnoreCaseOrderByNameAsc(String y);


   // List<MasReligion> findByStatusIgnoreCaseInOrderByLastChgDateDesc(List<String> y);

    List<MasReligion> findAllByOrderByStatusDescLastChgDateDesc();
}
