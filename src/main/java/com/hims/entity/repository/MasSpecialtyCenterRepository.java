package com.hims.entity.repository;

import com.hims.entity.MasSpecialtyCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasSpecialtyCenterRepository extends JpaRepository<MasSpecialtyCenter,Long> {
    List<MasSpecialtyCenter> findByStatusIgnoreCaseOrderByCenterNameAsc(String y);

    List<MasSpecialtyCenter> findAllByOrderByLastUpdateDateDesc();

    List<MasSpecialtyCenter> findAllByOrderByStatusDescLastUpdateDateDesc();
}
