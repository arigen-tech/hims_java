package com.hims.entity.repository;

import com.hims.entity.ObMasStationPresenting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ObMasStationPresentingRepository
        extends JpaRepository<ObMasStationPresenting, Long> {

    List<ObMasStationPresenting>
    findByStatusIgnoreCaseOrderByStationValueAsc(String status);

    List<ObMasStationPresenting>
    findAllByOrderByStatusDescLastUpdateDateDesc();
}
