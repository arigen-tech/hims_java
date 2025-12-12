package com.hims.entity.repository;

import com.hims.entity.MasPatientAcuity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasPatientAcuityRepository extends JpaRepository<MasPatientAcuity, Long> {
    List<MasPatientAcuity> findByStatusIgnoreCaseOrderByLastUpdateDateDesc(String y);

    List<MasPatientAcuity> findAllByOrderByLastUpdateDateDesc();
}
