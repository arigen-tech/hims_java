package com.hims.entity.repository;

import com.hims.entity.EntMasSeptum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EntMasSeptumRepository
        extends JpaRepository<EntMasSeptum, Long> {

    List<EntMasSeptum>
    findByStatusIgnoreCaseOrderBySeptumStatusAsc(String status);

    List<EntMasSeptum>
    findAllByOrderByStatusDescLastUpdateDateDesc();
}
