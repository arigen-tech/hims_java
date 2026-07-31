package com.hims.entity.repository;

import com.hims.entity.MasReceiptType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MasReceiptTypeRepository extends JpaRepository<MasReceiptType,Long> {
}
