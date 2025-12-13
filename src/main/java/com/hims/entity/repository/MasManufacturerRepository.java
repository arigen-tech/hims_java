package com.hims.entity.repository;

import com.hims.entity.MasManufacturer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface MasManufacturerRepository extends JpaRepository<MasManufacturer,Long> {

    List<MasManufacturer> findByStatusIgnoreCaseOrderByManufacturerNameAsc(String y);

    List<MasManufacturer> findByStatusIgnoreCaseInOrderByLastUpdatedDtDesc(List<String> y);
}
