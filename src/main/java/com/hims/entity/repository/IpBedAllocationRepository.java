package com.hims.entity.repository;

import com.hims.entity.IpBedAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IpBedAllocationRepository extends JpaRepository<IpBedAllocation,Long> {
}
