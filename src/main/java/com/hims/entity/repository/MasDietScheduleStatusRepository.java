package com.hims.entity.repository;

import com.hims.entity.MasDietScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasDietScheduleStatusRepository extends JpaRepository<MasDietScheduleStatus,Long> {
    List<MasDietScheduleStatus> findByStatusIgnoreCase(String y);

  //  List<MasDietScheduleStatus> findByStatusIgnoreCaseOrderByLastUpdateDateDesc(String y);

    List<MasDietScheduleStatus> findAllByOrderByLastUpdateDateDesc();

    List<MasDietScheduleStatus> findByStatusIgnoreCaseOrderByStatusNameAsc(String y);
}
