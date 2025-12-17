package com.hims.entity.repository;

import com.hims.entity.MasInvestigationCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasInvestigationCategoryRepository extends JpaRepository<MasInvestigationCategory,Long> {

  //  List<MasInvestigationCategory> findAllByOrderByLastChgDateDesc();

    List<MasInvestigationCategory> findAllByOrderByCategoryNameAsc();
}
