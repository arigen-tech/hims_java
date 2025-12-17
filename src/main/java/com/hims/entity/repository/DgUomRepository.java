package com.hims.entity.repository;

import com.hims.entity.DgUom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DgUomRepository extends JpaRepository<DgUom,Long> {

    List<DgUom> findByStatus(String status);


    List<DgUom> findByStatusOrderByNameAsc(String y);

    List<DgUom> findAllByOrderByLastChgDateDescLastChgTimeDesc();
}
