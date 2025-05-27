package com.hims.entity.repository;

import com.hims.entity.DgOrderHd;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LabHdRepository extends JpaRepository<DgOrderHd,Integer> {
    @Query(value = "SELECT MAX(CAST(SUBSTRING(order_no FROM '[0-9]+$') AS INTEGER)) FROM dg_orderhd WHERE order_no ~ '^ord-[0-9]+$'", nativeQuery = true)
    Integer findMaxOrderNo();
    Optional<DgOrderHd> findTopByOrderByIdDesc();
}
