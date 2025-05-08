package com.hims.entity.repository;

import com.hims.entity.DgOrderHd;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LabHdRepository extends JpaRepository<DgOrderHd,Integer> {
}
