package com.hims.entity.repository;

import com.hims.entity.MasVaccineMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasVaccineMasterRepository
        extends JpaRepository<MasVaccineMaster, Long> {

    List<MasVaccineMaster>
    findByStatusIgnoreCaseOrderByVaccineGroupAscDisplayOrderAsc(String status);

    List<MasVaccineMaster>
    findAllByOrderByStatusDescLastUpdateDateDesc();
}
