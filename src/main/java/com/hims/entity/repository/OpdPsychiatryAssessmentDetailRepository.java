package com.hims.entity.repository;

import com.hims.entity.OpdPsychiatryAssessmentDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface OpdPsychiatryAssessmentDetailRepository extends JpaRepository<OpdPsychiatryAssessmentDetail,Long> {
    List<OpdPsychiatryAssessmentDetail> findByAssessmentHeaderId_AssessmentHeaderIdIn(Collection<Long> assessmentHeaderIds);

    List<OpdPsychiatryAssessmentDetail> findByAssessmentHeaderId_AssessmentHeaderId(Long assessmentHeaderId);
}
