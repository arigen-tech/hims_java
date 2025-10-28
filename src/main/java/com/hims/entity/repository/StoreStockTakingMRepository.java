package com.hims.entity.repository;

import com.hims.entity.StoreStockTakingM;
import com.hims.entity.StoreStockTakingT;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreStockTakingMRepository extends JpaRepository<StoreStockTakingM,Long> {
    List<StoreStockTakingM> findByStatusIn(List<String> list);

    List<StoreStockTakingT> findByTakingMId(StoreStockTakingM m);

    List<StoreStockTakingM> findByStatusInAndHospitalIdIdAndDepartmentIdId(List<String> statusList, Long hospitalId, Long departmentId);
}
