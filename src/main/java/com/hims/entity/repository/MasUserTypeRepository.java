package com.hims.entity.repository;

import com.hims.entity.MasUserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MasUserTypeRepository extends JpaRepository<MasUserType, Long> {
}
