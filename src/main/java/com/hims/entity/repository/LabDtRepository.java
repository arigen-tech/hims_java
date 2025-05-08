package com.hims.entity.repository;

import com.hims.entity.DgOrderDt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LabDtRepository extends JpaRepository<DgOrderDt,Integer> {

}
