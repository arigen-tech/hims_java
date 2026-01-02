package com.hims.entity.repository;

import com.hims.entity.ObMasPvLiquor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ObMasPvLiquorRepository
        extends JpaRepository<ObMasPvLiquor, Long> {

    List<ObMasPvLiquor>
    findByStatusIgnoreCaseOrderByLiquorValueAsc(String status);

    List<ObMasPvLiquor>
    findAllByOrderByStatusDescLastUpdateDateDesc();
}
