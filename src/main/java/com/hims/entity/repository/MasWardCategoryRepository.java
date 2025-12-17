package com.hims.entity.repository;

import com.hims.entity.MasWardCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasWardCategoryRepository extends JpaRepository<MasWardCategory,Long> {

    List<MasWardCategory> findByStatus(String y);

    List<MasWardCategory> findByStatusOrderByLastUpdateDateDesc(String y);

    List<MasWardCategory> findAllByOrderByCategoryNameAsc();
}
