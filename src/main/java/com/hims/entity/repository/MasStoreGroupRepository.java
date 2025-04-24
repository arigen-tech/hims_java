package com.hims.entity.repository;

import com.hims.entity.MasStoreGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasStoreGroupRepository extends JpaRepository <MasStoreGroup, Integer> {

    List<MasStoreGroup> findByStatusIgnoreCase(String y);

    List<MasStoreGroup> findByStatusInIgnoreCase(List<String> y);


}

