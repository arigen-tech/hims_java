
package com.hims.entity.repository;

import com.hims.entity.StoreReturnM;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreReturnMRepository extends JpaRepository<StoreReturnM, Long> {
}