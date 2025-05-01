package com.hims.entity.repository;

import com.hims.entity.MasIdentificationType;
import com.hims.entity.MasMainChargeCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MasMainChargeCodeRepository extends JpaRepository<MasMainChargeCode, Long> {
    List<MasMainChargeCode> findByStatus(String status);
}
