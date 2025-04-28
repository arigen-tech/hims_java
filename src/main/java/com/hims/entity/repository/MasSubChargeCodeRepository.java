package com.hims.entity.repository;

import com.hims.entity.MasSubChargeCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MasSubChargeCodeRepository extends JpaRepository<MasSubChargeCode, Long> {
    List<MasSubChargeCode> findByStatus(String status);
}
