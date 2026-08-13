package com.hims.entity.repository;

import com.hims.entity.ItemClassBillSubcategoryMapping;
import com.hims.entity.MasItemClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemClassBillSubcategoryMappingRepository extends JpaRepository<ItemClassBillSubcategoryMapping,Long> {

    ItemClassBillSubcategoryMapping findByItemClass_ItemClassId(Integer itemClassId);
}
