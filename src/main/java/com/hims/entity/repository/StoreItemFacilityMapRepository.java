package com.hims.entity.repository;

import com.hims.entity.StoreItemFacilityMap;
import com.hims.projection.MasStoreItemFacilityProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreItemFacilityMapRepository extends JpaRepository<StoreItemFacilityMap,Long> {


    List<StoreItemFacilityMap> findByItemItemId(Long itemId);


    @Query(value = """

            SELECT 
            sf.item_id AS itemId,
            f.facility_id AS facilityId,
            f.facility_code AS facilityCode
        FROM store_item_facility_map sf
        JOIN mas_item_facility f 
            ON f.facility_id = sf.facility_id
        WHERE sf.item_id IN (:itemIds)
        """, nativeQuery = true)
    List<MasStoreItemFacilityProjection> findFacilityByItemIds(@Param("itemIds") List<Long> itemIds);
}