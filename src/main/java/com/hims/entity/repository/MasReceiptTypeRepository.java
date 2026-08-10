package com.hims.entity.repository;

import com.hims.entity.MasReceiptType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasReceiptTypeRepository extends JpaRepository<MasReceiptType,Long> {
    List<MasReceiptType> findByStatusIgnoreCaseOrderByReceiptTypeNameAsc(String lowerCase);

    List<MasReceiptType> findAllByOrderByStatusDescUpdatedAtDesc();
}
