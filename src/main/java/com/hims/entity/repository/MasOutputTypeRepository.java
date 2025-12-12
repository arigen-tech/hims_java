package com.hims.entity.repository;

import com.hims.entity.MasOutputType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasOutputTypeRepository extends JpaRepository<MasOutputType, Long> {
    List<MasOutputType> findByStatusIgnoreCaseOrderByLastUpdateDateDesc(String y);

    List<MasOutputType> findAllByOrderByLastUpdateDateDesc();
}
