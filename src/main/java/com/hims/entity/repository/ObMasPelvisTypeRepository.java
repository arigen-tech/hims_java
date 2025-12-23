package com.hims.entity.repository;

import com.hims.entity.ObMasPelvisType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ObMasPelvisTypeRepository
        extends JpaRepository<ObMasPelvisType, Long> {

    List<ObMasPelvisType>
    findByStatusIgnoreCaseOrderByPelvisTypeAsc(String status);

    List<ObMasPelvisType>
    findAllByOrderByStatusDescLastUpdateDateDesc();
}
