package com.hims.entity.repository;

import com.hims.entity.MasItemFacility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.management.loading.ClassLoaderRepository;
import java.util.List;

@Repository
public interface MasItemFacilityRepository extends JpaRepository<MasItemFacility,Long> {
    List<MasItemFacility> findByStatusIgnoreCaseOrderByFacilityNameAsc(String lowerCase);

    List<MasItemFacility> findAllByOrderByLastUpdateDateDesc();
}
