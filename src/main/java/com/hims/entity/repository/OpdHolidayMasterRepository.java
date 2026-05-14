package com.hims.entity.repository;

import com.hims.entity.OpdHolidayMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OpdHolidayMasterRepository extends JpaRepository<OpdHolidayMaster,Long> {


    List<OpdHolidayMaster> findByStatusIgnoreCaseOrderByHolidayNameAsc(String lowerCase);

    List<OpdHolidayMaster> findAllByOrderByLastUpdatedDtDesc();
}
