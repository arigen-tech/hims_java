package com.hims.entity.repository;

import com.hims.entity.DgOrderDt;
import com.hims.entity.DgOrderHd;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LabDtRepository extends JpaRepository<DgOrderDt,Integer> {
 List<DgOrderDt> findByOrderhdId(DgOrderHd hdObj);

}
