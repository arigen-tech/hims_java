package com.hims.entity.repository;

import com.hims.entity.StoreBalanceHd;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreBalanceHdRepository extends JpaRepository<StoreBalanceHd,Long> {
    List<StoreBalanceHd> findByStatus(String status);

    List<StoreBalanceHd> findByStatusIn(List<String> statusList);
}
