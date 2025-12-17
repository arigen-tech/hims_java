package com.hims.entity.repository;

import com.hims.entity.DischargeIcdCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DischargeIcdCodeRepository extends JpaRepository<DischargeIcdCode, Long> {
    List<DischargeIcdCode> findByOpdPatientDetailsIdAndVisitId(Long patientId,Long visitId);

    void deleteByOpdPatientDetailsId(Long opdPatientDetailsId);
}
