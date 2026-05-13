package com.hims.entity.repository;

import com.hims.entity.RadOrderHd;
import com.hims.entity.Visit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RadOrderHdRepository extends JpaRepository<RadOrderHd, Long> {








    List<RadOrderHd> findAllByVisit_Id(Long visitId);
}
