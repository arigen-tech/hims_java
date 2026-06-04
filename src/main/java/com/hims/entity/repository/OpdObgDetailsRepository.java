package com.hims.entity.repository;

import com.hims.entity.OpdObgDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OpdObgDetailsRepository extends JpaRepository<OpdObgDetails,Long> {
}
