package com.hims.entity.repository;

import com.hims.entity.GynMasPapSmear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GynMasPapSmearRepository
        extends JpaRepository<GynMasPapSmear, Long> {

    List<GynMasPapSmear>
    findByStatusIgnoreCaseOrderByPapResultAsc(String status);

    List<GynMasPapSmear>
    findAllByOrderByStatusDescLastUpdateDateDesc();
}
