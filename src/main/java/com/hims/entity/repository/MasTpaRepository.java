package com.hims.entity.repository;

import com.hims.entity.MasTpa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasTpaRepository extends JpaRepository<MasTpa,Long> {
    List<MasTpa> findAllByOrderByStatusDescLastChgDateDesc();

    List<MasTpa> findByStatusIgnoreCaseOrderByTpaNameAsc(String lowerCase);
}
