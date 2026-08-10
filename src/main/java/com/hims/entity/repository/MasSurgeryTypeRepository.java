package com.hims.entity.repository;

import com.hims.entity.MasSurgeryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasSurgeryTypeRepository extends JpaRepository<MasSurgeryType, Long> {

    @Query("""
    SELECT s FROM MasSurgeryType s
    WHERE LOWER(s.status) = LOWER(:status)
    ORDER BY s.surgeryTypeName ASC
""")
    List<MasSurgeryType> findActive(@Param("status") String status);

    @Query("""
    SELECT s FROM MasSurgeryType s
    ORDER BY s.lastUpdatedDate DESC
""")
    List<MasSurgeryType> findActiveAndDeactive();
}