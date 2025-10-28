package com.hims.entity.repository;

import com.hims.entity.StoreInternalIndentM;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreInternalIndentMRepository extends JpaRepository<StoreInternalIndentM,Long> {
}
