package com.hims.entity.repository;

import com.hims.entity.MasIpdPaymentStatus;
import lombok.Data;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MasIpdPaymentStatusRepository extends JpaRepository<MasIpdPaymentStatus,Long> {
}
