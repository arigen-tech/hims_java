package com.hims.entity.repository;

import com.hims.entity.ObMasImmunisedStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ObMasImmunisedStatusRepository
        extends JpaRepository<ObMasImmunisedStatus, Long> {

    List<ObMasImmunisedStatus>
    findByStatusIgnoreCaseOrderByImmunisationValueAsc(String status);

    List<ObMasImmunisedStatus>
    findAllByOrderByStatusDescLastUpdateDateDesc();
}

