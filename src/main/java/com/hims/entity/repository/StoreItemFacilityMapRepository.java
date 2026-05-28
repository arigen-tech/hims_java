package com.hims.entity.repository;

import com.hims.entity.StoreItemFacilityMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StoreItemFacilityMapRepository extends JpaRepository<StoreItemFacilityMap,Long> {


    List<StoreItemFacilityMap> findByItemItemId(Long itemId);


}
