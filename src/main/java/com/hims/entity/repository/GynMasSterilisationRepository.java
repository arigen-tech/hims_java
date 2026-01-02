package com.hims.entity.repository;

import com.hims.entity.GynMasSterilisation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GynMasSterilisationRepository
        extends JpaRepository<GynMasSterilisation, Long> {

    List<GynMasSterilisation>
    findByStatusIgnoreCaseOrderBySterilisationTypeAsc(String status);

    List<GynMasSterilisation>
    findAllByOrderByStatusDescLastUpdateDateDesc();
}
