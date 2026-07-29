package com.hims.entity.repository;

import com.hims.entity.MasDischargeReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MasDischargeReasonRepository extends JpaRepository<MasDischargeReason,Long> {
}
