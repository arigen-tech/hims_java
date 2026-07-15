package com.hims.entity.repository;

import com.hims.entity.MasBed;
import com.hims.projection.BedStatusCountProjection;
import com.hims.response.TotalBedCountResponse;
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
            WHERE mr.ward_id = :wardId
            """, nativeQuery = true)
    BedStatusCountProjection getBedStatusCount(
            @Param("wardId") Long wardId,
            @Param("available") String available,
            @Param("cleaning") String cleaning,
            @Param("occupied") String occupied
            );


    @Query("""
            SELECT b
            FROM MasBed b
            WHERE b.roomId.roomId = :roomId
              AND LOWER(b.status) =:status
              AND b.bedStatusId.bedStatusId =:bedStatusId
            ORDER BY b.bedNumber
            """)
    List<MasBed> findAllActiveBedsByRoomId(@Param("roomId") Long roomId,
                                           @Param("bedStatusId") Long bedStatusId,
                                           @Param("status") String status);



    @Query("""
    SELECT new com.hims.response.TotalBedCountResponse(
        COUNT(b.bedId),

        SUM(
            CASE
                WHEN b.bedStatusId.bedStatusId = :bedStatusId
                THEN 1L
                ELSE 0L
            END
        ),

        SUM(
            CASE
                WHEN b.bedStatusId.bedStatusId = :bedStatusOccupiedId
                THEN 1L
                ELSE 0L
            END
        ),

        w.wardName
    )
    FROM MasBed b
    JOIN b.roomId r
    JOIN r.masWard w
    WHERE w.wardId = :wardId
    GROUP BY w.wardId, w.wardName
""")
    TotalBedCountResponse getTotalBedCountByDepartmentId(
            @Param("wardId") Long wardId,
            @Param("bedStatusId") Long bedStatusId,
            @Param("bedStatusOccupiedId") Long bedStatusOccupiedId
    );
}
