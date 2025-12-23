package com.hims.entity.repository;

import com.hims.entity.ObMasConception;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ObMasConceptionRepository
        extends JpaRepository<ObMasConception, Long> {

    List<ObMasConception> findByStatusIgnoreCaseOrderByConceptionTypeAsc(String status);

    List<ObMasConception> findAllByOrderByStatusDescLastUpdateDateDesc();
}
