package com.hims.entity.repository;


import com.hims.entity.MasRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasRoomRepo extends JpaRepository<MasRoom,Long> {

    List<MasRoom> findByStatusIgnoreCaseOrderByLastUpdatedDateDesc(String status);

    List<MasRoom> findByStatusIgnoreCaseInOrderByLastUpdatedDateDesc(List<String> statuses);


}
