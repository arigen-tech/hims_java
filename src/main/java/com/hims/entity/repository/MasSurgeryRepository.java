package com.hims.entity.repository;

import com.hims.entity.MasSurgery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasSurgeryRepository extends JpaRepository<MasSurgery,Long> {



    @Query("""
    SELECT s FROM MasSurgery s
    LEFT JOIN FETCH s.department d
    WHERE LOWER(s.status) = LOWER(:status)
    ORDER BY s.surgeryName ASC
""")
    List<MasSurgery> findActive(@Param("status") String status);
    @Query("""
    SELECT s FROM MasSurgery s
    LEFT JOIN FETCH s.department d
    ORDER BY  s.lastUpdatedDate DESC
""")
    List<MasSurgery> findActiveAndDeactive();
}
