package com.hims.entity.repository;


import com.hims.entity.MasRoom;
import com.hims.response.IpdRoomResponse;
import com.hims.response.IpdWardResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasRoomRepo extends JpaRepository<MasRoom,Long> {

 //   List<MasRoom> findByStatusIgnoreCaseOrderByLastUpdatedDateDesc(String status);

 //   List<MasRoom> findByStatusIgnoreCaseInOrderByLastUpdatedDateDesc(List<String> statuses);


    List<MasRoom> findByStatusIgnoreCaseOrderByRoomNameAsc(String y);

    List<MasRoom> findAllByOrderByStatusDescLastUpdatedDateDesc();


    @Query("""
        SELECT new com.hims.response.IpdRoomResponse(
            r.roomId,
            r.roomName,
            COUNT(DISTINCT b.bedId)
        )
        FROM MasRoom r
        LEFT JOIN MasBed b
            ON b.roomId.roomId = r.roomId
            AND LOWER(b.status) =:status
             AND b.bedStatusId.bedStatusId =:bedStatusId
        WHERE r.masWard.wardId = :wardId
          AND LOWER(r.status) =:status
        GROUP BY r.roomId, r.roomName
        ORDER BY r.roomName ASC
    """)
    List<IpdRoomResponse> getRoomByWard(
            @Param("wardId") Long wardId,
            @Param("bedStatusId") Long bedStatusId,
            @Param("status") String status
    );
}
