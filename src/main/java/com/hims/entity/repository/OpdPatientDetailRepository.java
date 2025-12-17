package com.hims.entity.repository;

import com.hims.entity.OpdPatientDetail;
import com.hims.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OpdPatientDetailRepository extends JpaRepository<OpdPatientDetail, Long> {
    OpdPatientDetail findByVisitId(Long id);

    OpdPatientDetail findTopByPatientOrderByOpdPatientDetailsIdDesc(Patient patient);
}
