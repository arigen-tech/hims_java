package com.hims.entity.repository;

import com.hims.entity.OpdOpthDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OpdOpthDetailsRepository extends JpaRepository<OpdOpthDetails,Long> {
}
