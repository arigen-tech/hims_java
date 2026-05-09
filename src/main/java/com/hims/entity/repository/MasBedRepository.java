package com.hims.entity.repository;

import com.hims.entity.MasBed;
import com.hims.projection.BedStatusCountProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasBedRepository extends JpaRepository<MasBed,Long> {
    List<MasBed> findByStatusIgnoreCaseIn(List<String> y);

    List<MasBed> findByStatusIgnoreCase(String y);

    List<MasBed> findAllByOrderByStatusDescLastUpdateDateDesc();
    @Query(value = """
            SELECT 
                COALESCE(SUM(CASE 
                    WHEN LOWER(mbs.bed_status_name) = :available
                    THEN 1 ELSE 0 END),0) AS available,

                COALESCE(SUM(CASE 
                    WHEN LOWER(mbs.bed_status_name) = :cleaning 
                    THEN 1 ELSE 0 END),0) AS cleaning,

                COALESCE(SUM(CASE 
                    WHEN LOWER(mbs.bed_status_name) = :occupied 
                    THEN 1 ELSE 0 END),0) AS occupied

            FROM mas_room mr
            JOIN mas_bed mb 
                ON mr.room_id = mb.room_id
            JOIN mas_bed_status mbs 
                ON mb.bed_status_id = mbs.bed_status_id
            WHERE mr.department_id = :departmentId
            """, nativeQuery = true)
    BedStatusCountProjection getBedStatusCount(
            @Param("departmentId") Long departmentId,
            @Param("available") String available,
            @Param("cleaning") String cleaning,
            @Param("occupied") String occupied
            );

}
