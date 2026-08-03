package com.hims.entity.repository;

import com.hims.entity.MasStoreSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MasStoreSectionRepository extends JpaRepository<MasStoreSection,Integer> {

//    List<MasStoreSection> findByStatusIgnoreCase(String y);
//
//    List<MasStoreSection> findByStatusInIgnoreCase(List<String> y);

  //  List<MasStoreSection> findByMasItemTypeId(int id);

   // List<MasStoreSection> findByStatusInIgnoreCaseOrderByLastChgDateDescLastChgTimeDesc(List<String> y);

    List<MasStoreSection> findByStatusIgnoreCaseOrderBySectionNameAsc(String y);


    List<MasStoreSection> findByMasItemTypeIdOrderBySectionNameAsc(int id);

    List<MasStoreSection> findAllByOrderByStatusDescLastChgDateDescLastChgTimeDesc();

    List<MasStoreSection> findByMasItemType_CodeOrderBySectionNameAsc(String itemTypeCode);

    Optional<MasStoreSection> findBySectionCode(String sectionCode);
}
