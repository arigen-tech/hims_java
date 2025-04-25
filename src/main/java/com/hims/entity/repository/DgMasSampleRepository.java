package com.hims.entity.repository;

import com.hims.entity.DgMasSample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DgMasSampleRepository extends JpaRepository<DgMasSample,Long> {

    List<DgMasSample> findByStatus(String y);
}
