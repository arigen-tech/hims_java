package com.hims.entity.repository;

import com.hims.entity.OpdPsychiatryAssessmentDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OpdPsychiatryAssessmentDetailRepository extends JpaRepository<OpdPsychiatryAssessmentDetail,Long> {
}
