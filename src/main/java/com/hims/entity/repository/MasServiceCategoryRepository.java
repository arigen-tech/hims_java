package com.hims.entity.repository;

import com.hims.entity.MasServiceCategory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MasServiceCategoryRepository extends JpaRepository<MasServiceCategory, Long> {
    List<MasServiceCategory> findAllByStatus(String status);
    @Query(value = "SELECT service_cate_code FROM mas_service_category WHERE service_cate_code IS NOT NULL ORDER BY service_cate_code DESC LIMIT 1", nativeQuery = true)
    String findTopServiceCateCode();

   MasServiceCategory  findByServiceCateCode(String serviceCateCode );



}
