package com.hims.entity.repository;

import com.hims.entity.MasBrand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasBrandRepository extends JpaRepository<MasBrand,Integer> {
    List<MasBrand> findByStatusIgnoreCase(String y);

    List<MasBrand> findByStatusInIgnoreCase(List<String> y);
}
