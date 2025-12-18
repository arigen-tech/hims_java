package com.hims.entity.repository;

import com.hims.entity.MasIntakeItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasIntakeItemRepository  extends JpaRepository<MasIntakeItem, Long> {
  //  List<MasIntakeItem> findByStatusIgnoreCaseOrderByLastUpdateDateDesc(String y);

   // List<MasIntakeItem> findAllByOrderByLastUpdateDateDesc();

    List<MasIntakeItem> findByStatusIgnoreCaseOrderByIntakeItemNameAsc(String y);

    List<MasIntakeItem> findAllByOrderByStatusDescLastUpdateDateDesc();
}
