package com.hims.entity.repository;

import com.hims.entity.IpVitals;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IpVitalsRepository extends JpaRepository<IpVitals,Long> {
}
