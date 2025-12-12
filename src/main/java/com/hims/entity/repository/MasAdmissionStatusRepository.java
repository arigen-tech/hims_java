package com.hims.entity.repository;

import com.hims.entity.MasAdmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasAdmissionStatusRepository extends JpaRepository<MasAdmissionStatus, Long> {
    List<MasAdmissionStatus> findByStatusIgnoreCaseOrderByLastUpdateDateDesc(String y);

    List<MasAdmissionStatus> findAllByOrderByLastUpdateDateDesc();
}
