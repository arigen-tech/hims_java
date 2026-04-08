package com.hims.entity.repository;

import com.hims.entity.MasVisitType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasVisitTypeRepository extends JpaRepository<MasVisitType,Long> {
    List<MasVisitType> findAllByOrderByStatusDescLastChangedDateDesc();

    List<MasVisitType> findByStatusIgnoreCaseOrderByVisitTypeNameAsc(String lowerCase);
}
