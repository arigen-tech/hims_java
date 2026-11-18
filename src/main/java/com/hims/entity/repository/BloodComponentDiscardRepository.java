package com.hims.entity.repository;


import com.hims.entity.BloodComponentDiscard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BloodComponentDiscardRepository
        extends JpaRepository<BloodComponentDiscard, Long> {

    boolean existsByInventory_InventoryId(Long inventoryId);

    List<BloodComponentDiscard> findAllByInventory_InventoryIdIn(
            List<Long> inventoryIds
    );

}
