package com.hims.entity.repository;

import com.hims.entity.MasIcd;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface MasIcdRepository extends JpaRepository<MasIcd, Long> {
//    List<MasIcd> findByStatusIgnoreCase(String status);
//
//    List<MasIcd> findByStatusInIgnoreCase(List<String> statuses);

    Page<MasIcd> findByStatusIgnoreCase(String status, Pageable pageable);

    Page<MasIcd> findByStatusInIgnoreCase(List<String> statuses, Pageable pageable);
}
