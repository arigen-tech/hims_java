package com.hims.entity.repository;

import com.hims.entity.MasItemCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasItemCategoryRepository extends JpaRepository<MasItemCategory,Integer> {


  //  List<MasItemCategory> findByStatusIgnoreCaseOrderByLastChgDateDescLastChgTimeDesc(String status);

  //  List<MasItemCategory> findByStatusInIgnoreCaseOrderByLastChgDateDescLastChgTimeDesc(List<String> statuses);

   // List<MasItemCategory> findByMasStoreSectionSectionId(int id);

    List<MasItemCategory> findByStatusIgnoreCaseOrderByItemCategoryNameAsc(String y);

    List<MasItemCategory> findByMasStoreSectionSectionIdOrderByItemCategoryNameAsc(int id);

    List<MasItemCategory> findAllByOrderByStatusDescLastChgDateDescLastChgTimeDesc();

    @Query("""
            SELECT m
            FROM MasItemCategory m
            ORDER BY m.status DESC,
                     m.lastChgDate DESC,
                     m.lastChgTime DESC
            """)
    List<MasItemCategory> getAllMasItemCategoryData();
}
