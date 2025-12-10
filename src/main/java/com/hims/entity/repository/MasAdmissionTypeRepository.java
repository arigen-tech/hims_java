package com.hims.entity.repository;

import com.hims.entity.MasAdmissionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasAdmissionTypeRepository extends JpaRepository<MasAdmissionType, Long> {
    List<MasAdmissionType> findByStatusIgnoreCase(String y);

    List<MasAdmissionType> findByStatusIgnoreCaseOrderByLastUpdateDateDesc(String y);

    List<MasAdmissionType> findAllByOrderByLastUpdateDateDesc();
}
