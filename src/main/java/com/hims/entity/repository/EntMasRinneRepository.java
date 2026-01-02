package com.hims.entity.repository;

import com.hims.entity.EntMasRinne;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EntMasRinneRepository
        extends JpaRepository<EntMasRinne, Long> {

    List<EntMasRinne>
    findByStatusIgnoreCaseOrderByRinneResultAsc(String status);

    List<EntMasRinne>
    findAllByOrderByStatusDescLastUpdateDateDesc();
}
