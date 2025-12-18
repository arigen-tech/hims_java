package com.hims.entity.repository;

import com.hims.entity.MasBed;
import com.hims.entity.MasBedStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasBedStatusRepo extends JpaRepository<MasBedStatus,Long> {

   // List<MasBedStatus> findByStatusIgnoreCaseOrderByLastUpdateDateDesc(String status);

    List<MasBedStatus> findByStatusIgnoreCaseInOrderByLastUpdateDateDesc(List<String> statuses);

    List<MasBedStatus> findByStatusIgnoreCaseOrderByBedStatusNameAsc(String y);

    List<MasBedStatus> findAllByOrderByStatusDescLastUpdateDateDesc();
}
