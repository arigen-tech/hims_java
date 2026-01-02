package com.hims.entity.repository;

import com.hims.entity.EntMasWeber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EntMasWeberRepository
        extends JpaRepository<EntMasWeber, Long> {

    List<EntMasWeber>
    findByStatusIgnoreCaseOrderByWeberResultAsc(String status);

    List<EntMasWeber>
    findAllByOrderByStatusDescLastUpdateDateDesc();
}
