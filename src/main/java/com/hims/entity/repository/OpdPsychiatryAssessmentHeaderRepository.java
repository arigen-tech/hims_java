package com.hims.entity.repository;

import com.hims.entity.OpdPsychiatryAssessmentHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public interface OpdPsychiatryAssessmentHeaderRepository extends JpaRepository<OpdPsychiatryAssessmentHeader,Long> {
    Page<OpdPsychiatryAssessmentHeader> findByPatient_IdOrderByAssessmentDateDesc(
            Long patientId,
            Pageable pageable
    );
}
