package com.hims.entity.repository;

import com.hims.entity.MasIpNursingAssessmentValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasIpNursingAssessmentValueRepository extends JpaRepository<MasIpNursingAssessmentValue,Long> {
    List<MasIpNursingAssessmentValue> findByStatusIgnoreCaseOrderByCategoryCodeAscDisplayOrderAsc(String lowerCase);

    List<MasIpNursingAssessmentValue> findAllByOrderByStatusDescCategoryCodeAscDisplayOrderAsc();
}
