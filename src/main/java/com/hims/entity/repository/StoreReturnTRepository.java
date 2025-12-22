package com.hims.entity.repository;

import com.hims.entity.StoreReturnT;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreReturnTRepository extends JpaRepository<StoreReturnT, Long> {
}
