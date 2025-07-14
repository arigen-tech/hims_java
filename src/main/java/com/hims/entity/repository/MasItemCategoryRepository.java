package com.hims.entity.repository;

import com.hims.entity.MasItemCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasItemCategoryRepository extends JpaRepository<MasItemCategory,Integer> {


    List<MasItemCategory> findByStatusIgnoreCase(String y);

    List<MasItemCategory> findByStatusInIgnoreCase(List<String> y);

    List<MasItemCategory> findByMasStoreSectionSectionId(int id);
}
