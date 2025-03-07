package com.hims.entity.repository;

import com.hims.entity.MasIdentificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MasIdentificationTypeRepository extends JpaRepository<MasIdentificationType, Long> {
}
