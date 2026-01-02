package com.hims.entity.repository;

import com.hims.entity.ObMasBookedStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ObMasBookedStatusRepository
        extends JpaRepository<ObMasBookedStatus, Long> {

    List<ObMasBookedStatus>
    findByStatusIgnoreCaseOrderByBookedStatusAsc(String status);

    List<ObMasBookedStatus>
    findAllByOrderByStatusDescLastUpdateDateDesc();
}
