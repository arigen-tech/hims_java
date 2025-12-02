package com.hims.entity.repository;

import com.hims.entity.MasRoomCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MasRoomCategoryRepo extends JpaRepository<MasRoomCategory,Long> {

    List<MasRoomCategory> findByStatusIgnoreCaseOrderByLastUpdatedDateDesc(String status);

    List<MasRoomCategory> findByStatusIgnoreCaseInOrderByLastUpdatedDateDesc(List<String> statuses);
}
