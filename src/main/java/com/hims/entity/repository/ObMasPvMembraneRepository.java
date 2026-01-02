package com.hims.entity.repository;

import com.hims.entity.ObMasPvMembrane;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ObMasPvMembraneRepository
        extends JpaRepository<ObMasPvMembrane, Long> {

    List<ObMasPvMembrane>
    findByStatusIgnoreCaseOrderByMembraneStatusAsc(String status);

    List<ObMasPvMembrane>
    findAllByOrderByStatusDescLastUpdateDateDesc();
}
