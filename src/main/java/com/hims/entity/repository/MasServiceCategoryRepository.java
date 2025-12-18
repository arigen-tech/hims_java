package com.hims.entity.repository;

import com.hims.entity.MasServiceCategory;
import com.hims.entity.MasServiceOpd;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MasServiceCategoryRepository extends JpaRepository<MasServiceCategory, Long> {
    List<MasServiceCategory> findAllByStatus(String status);
    @Query(value = "SELECT service_cate_code FROM mas_service_category WHERE service_cate_code IS NOT NULL ORDER BY service_cate_code DESC LIMIT 1", nativeQuery = true)
    String findTopServiceCateCode();

  //  List<MasServiceCategory> findAllByStatusOrderByLastChgDtDesc(String status);

    List<MasServiceCategory> findAllByOrderByLastChgDtDesc();

   MasServiceCategory  findByServiceCateCode(String serviceCateCode );

    Optional<MasServiceCategory> findBySacCode(String cateCode);


    List<MasServiceCategory> findAllByStatusOrderByServiceCatNameAsc(String y);

    List<MasServiceCategory> findAllByOrderByStatusDescLastChgDtDesc();
}
