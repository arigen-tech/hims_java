package com.hims.entity.repository;

import com.hims.entity.MasDrugSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasDrugScheduleRepository extends JpaRepository<MasDrugSchedule,String> {
    List<MasDrugSchedule> findByStatusIgnoreCaseOrderByScheduleCodeAsc(String lowerCase);

    List<MasDrugSchedule> findAllByOrderByLastUpdateDateDesc();
}
