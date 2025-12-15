package com.hims.entity.repository;

import com.hims.entity.MasDesignation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasDesignationRepository extends JpaRepository<MasDesignation,Long> {
    List<MasDesignation> findByStatusIgnoreCaseOrderByDesignationNameAsc(String y);

    List<MasDesignation> findAllByOrderByLastUpdateDateDesc();
}
