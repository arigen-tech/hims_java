package com.hims.entity.repository;


import com.hims.entity.Patient;
import com.hims.entity.PatientAbhaMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientAbhaMappingRepository extends JpaRepository<PatientAbhaMapping,Long> {
    Optional<PatientAbhaMapping> findByPatient(Patient patient);
}
