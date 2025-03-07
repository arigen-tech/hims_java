package com.hims.entity.repository;

import com.hims.entity.MasEmploymentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MasEmploymentTypeRepository extends JpaRepository<MasEmploymentType, Long> {
}
