package com.hims.entity.repository;

import com.hims.entity.MasServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MasServiceCategoryRepository extends JpaRepository<MasServiceCategory, Long> {
    List<MasServiceCategory> findAllByStatus(String status);
}
