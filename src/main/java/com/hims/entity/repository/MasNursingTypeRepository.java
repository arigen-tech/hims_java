package com.hims.entity.repository;

import com.hims.entity.MasNursingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasNursingTypeRepository  extends JpaRepository<MasNursingType, Long> {
    List<MasNursingType> findByStatusIgnoreCaseOrderByNursingTypeNameAsc(String y);

    List<MasNursingType> findAllByOrderByStatusDescLastUpdateDateDesc();
}
