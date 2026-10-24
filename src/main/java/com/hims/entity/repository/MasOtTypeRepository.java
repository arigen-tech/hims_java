package com.hims.entity.repository;

import com.hims.entity.MasOtType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasOtTypeRepository extends JpaRepository<MasOtType, Long> {

    List<MasOtType> findByStatusIgnoreCaseOrderByOtTypeNameAsc(String status);

    List<MasOtType> findAllByOrderByStatusDescOtTypeNameAsc();
}
