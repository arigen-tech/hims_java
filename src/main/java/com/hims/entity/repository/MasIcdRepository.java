package com.hims.entity.repository;

import com.hims.constants.AppConstants;
import com.hims.entity.MasIcd;
import com.hims.response.MasIcdResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


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
        (:flag = 0 AND LOWER(m.status) IN (LOWER(:statusY), LOWER(:statusN)))
        OR 
        (:flag = 1 AND LOWER(m.status) = LOWER(:statusY))
      )
    AND (
        LOWER(m.icdCode) LIKE LOWER(:search)
        OR LOWER(m.icdName) LIKE LOWER(:search)
    )
""")
    Page<MasIcd> searchICD(
            @Param("flag") int flag,
            @Param("search") String search,
            @Param("statusY") String statusY,
            @Param("statusN") String statusN,
            Pageable pageable
    );

    @Query("""
SELECT new com.hims.response.MasIcdResponse(
    m.icdId,
    m.icdCode,
    m.icdName
)
FROM MasIcd m
WHERE
(
    (:flag = 0 AND LOWER(m.status) IN (LOWER(:statusY), LOWER(:statusN)))
    OR
    (:flag = 1 AND LOWER(m.status) = LOWER(:statusY))
)
AND
(
    :search IS NULL 
    OR :search = '' 
    OR TRIM(:search) = ''
    OR LOWER(m.icdCode) LIKE LOWER(CONCAT('%', :search, '%'))
    OR LOWER(m.icdName) LIKE LOWER(CONCAT('%', :search, '%'))
)
""")
    Page<MasIcdResponse> findAllIcdWithFilter(
            @Param("flag") int flag,
            @Param("search") String search,
            @Param("statusY") String statusY,
            @Param("statusN") String statusN,
            Pageable pageable
    );

}
