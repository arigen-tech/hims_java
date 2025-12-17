package com.hims.entity.repository;

import com.hims.entity.MasEmploymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasEmploymentTypeRepository extends JpaRepository<MasEmploymentType, Long> {

//    List<MasEmploymentType> findByStatusIgnoreCase(String y);
//
//    List<MasEmploymentType> findByStatusInIgnoreCase(List<String> y);

    List<MasEmploymentType> findByStatusIgnoreCaseOrderByEmploymentTypeAsc(String y);

    List<MasEmploymentType> findByStatusInIgnoreCaseOrderByLastChangedDateDesc(List<String> y);
}
