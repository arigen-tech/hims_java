package com.hims.entity.repository;

import com.hims.entity.MasToothMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasToothMasterRepository  extends JpaRepository<MasToothMaster, Long> {

    List<MasToothMaster> findByStatusIgnoreCaseOrderByDisplayOrderAsc(String y);

    List<MasToothMaster> findAllByOrderByStatusDescLastUpdateDateDesc();
}
