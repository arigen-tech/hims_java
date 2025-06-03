package com.hims.entity.repository;

import com.hims.entity.MasItemClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasItemClassRepository extends JpaRepository<MasItemClass,Integer> {

    List<MasItemClass> findByStatusIgnoreCase(String y);

    List<MasItemClass> findByStatusInIgnoreCase(List<String> y);
}
