package com.hims.entity.repository;

import com.hims.entity.DgFixedValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DgFixedValueRepository extends JpaRepository<DgFixedValue,Long> {

}
