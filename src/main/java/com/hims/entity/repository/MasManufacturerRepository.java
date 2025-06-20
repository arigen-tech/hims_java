package com.hims.entity.repository;

import com.hims.entity.MasManufacturer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface MasManufacturerRepository extends JpaRepository<MasManufacturer,Long> {
    List<MasManufacturer> findByStatusIgnoreCase(String y);

    List<MasManufacturer> findByStatusInIgnoreCase(List<String> y);
}
