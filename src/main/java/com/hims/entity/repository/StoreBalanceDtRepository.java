package com.hims.entity.repository;

import com.hims.entity.StoreBalanceDt;
import com.hims.entity.StoreBalanceHd;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreBalanceDtRepository extends JpaRepository<StoreBalanceDt,Long> {
    void deleteByBalanceMId(StoreBalanceHd updatedHd);

    List<StoreBalanceDt> findByBalanceMId(StoreBalanceHd hd);
}
