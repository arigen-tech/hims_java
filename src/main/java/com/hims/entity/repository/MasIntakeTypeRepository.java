package com.hims.entity.repository;

import com.hims.entity.MasIntakeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasIntakeTypeRepository extends JpaRepository<MasIntakeType, Long> {
  //  List<MasIntakeType> findByStatusIgnoreCaseOrderByLastUpdateDateDesc(String y);

    List<MasIntakeType> findAllByOrderByStatusDescLastUpdateDateDesc();


    List<MasIntakeType> findByStatusIgnoreCaseOrderByIntakeTypeNameAsc(String y);
}
