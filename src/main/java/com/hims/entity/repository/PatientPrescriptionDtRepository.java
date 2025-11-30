package com.hims.entity.repository;

import com.hims.entity.PatientPrescriptionDt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PatientPrescriptionDtRepository extends JpaRepository<PatientPrescriptionDt, Long> {
    List<PatientPrescriptionDt> findByPrescriptionHdId(Long prescriptionHdId);

    void deleteByPrescriptionHdId(Long prescriptionHdId);
}
