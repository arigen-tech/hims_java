package com.hims.entity.repository;

import com.hims.entity.MasSurgery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasSurgeryRepository extends JpaRepository<MasSurgery, Long> {

    @Query("""
    SELECT s FROM MasSurgery s
    WHERE LOWER(s.status) = LOWER(:status)
    ORDER BY s.surgeryName ASC
""")
    List<MasSurgery> findActive(@Param("status") String status);

    @Query("""
    SELECT s FROM MasSurgery s
    ORDER BY s.lastUpdatedDate DESC
""")
    List<MasSurgery> findActiveAndDeactive();

    @Query("""
    SELECT s FROM MasSurgery s
    WHERE s.status = :status
    AND (:search IS NULL OR 
         LOWER(s.surgeryName) LIKE LOWER(CONCAT('%', :search, '%')))
""")
    Page<MasSurgery> searchMasSurgery(@Param("status") String status,
                                      @Param("search") String search,
                                      Pageable pageable);

   @Query("""
    SELECT s FROM MasSurgery s
    WHERE LOWER(s.surgeryLevel) = LOWER(:surgeryLevel)
    AND (LOWER(s.status) = LOWER(:activeStatus))
    ORDER BY s.surgeryName ASC
""")
   List<MasSurgery> findBySurgeryLevelAndFlag(@Param("surgeryLevel") String surgeryLevel,
                                              @Param("activeStatus") String activeStatus);
}