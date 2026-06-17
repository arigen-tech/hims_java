package com.hims.entity.repository;

import com.hims.entity.PacsHmisStudy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PacsHmisStudyRepository extends JpaRepository<PacsHmisStudy, Long> {

    List<PacsHmisStudy> findAllByUhidAndOrderNo(String uhid, String orderNo);

}
