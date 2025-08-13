package com.hims.entity.repository;

import com.hims.entity.DgSampleCollectionHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DgSampleCollectionHeaderRepository extends JpaRepository<DgSampleCollectionHeader,Long> {
}
