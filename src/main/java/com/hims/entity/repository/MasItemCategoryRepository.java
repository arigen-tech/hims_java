package com.hims.entity.repository;

import com.hims.entity.MasItemCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasItemCategoryRepository extends JpaRepository<MasItemCategory,Integer> {


    List<MasItemCategory> findByStatusIgnoreCaseOrderByLastChgDateDescLastChgTimeDesc(String status);

    List<MasItemCategory> findByStatusInIgnoreCaseOrderByLastChgDateDescLastChgTimeDesc(List<String> statuses);

    List<MasItemCategory> findByMasStoreSectionSectionId(int id);

    List<MasItemCategory> findByStatusIgnoreCaseOrderByItemCategoryNameAsc(String y);
}
