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
            w.roomId,
            w.roomName
        )
        FROM MasRoom w
        WHERE w.masWard.wardId = :wardId
          AND LOWER(w.status) =:status
        ORDER BY w.roomName ASC
    """)
    List<IpdRoomResponse> getRoomByWard(
            @Param("wardId") Long wardId,
            @Param("status") String status
    );
}
