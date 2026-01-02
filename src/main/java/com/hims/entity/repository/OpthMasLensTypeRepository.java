package com.hims.entity.repository;

import com.hims.entity.OpthMasLensType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpthMasLensTypeRepository
        extends JpaRepository<OpthMasLensType, Long> {

    List<OpthMasLensType> findByStatusIgnoreCaseOrderByLensTypeAsc(String status);

    List<OpthMasLensType> findAllByOrderByStatusDescLastUpdateDateDesc();
}
