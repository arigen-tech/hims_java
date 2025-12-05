package com.hims.entity.repository;

import com.hims.entity.MasIcd;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.util.List;

public interface MasIcdRepository extends JpaRepository<MasIcd, Long> {
//    List<MasIcd> findByStatusIgnoreCase(String status);
//
//    List<MasIcd> findByStatusInIgnoreCase(List<String> statuses);

    Page<MasIcd> findByStatusIgnoreCase(String status, Pageable pageable);

    Page<MasIcd> findByStatusInIgnoreCase(List<String> statuses, Pageable pageable);

    @Query("""
    SELECT m FROM MasIcd m
    WHERE 
      (
        (:flag = 0 AND LOWER(m.status) IN ('y', 'n'))
        OR 
        (:flag = 1 AND LOWER(m.status) = 'y')
      )
    AND (
        LOWER(m.icdCode) LIKE LOWER(:search)
        OR LOWER(m.icdName) LIKE LOWER(:search)
    )
""")
    Page<MasIcd> searchICD(int flag, String search, Pageable pageable);


}
