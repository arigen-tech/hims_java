package com.hims.entity.repository;

import com.hims.entity.IpMarDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IpMarDetailsRepository extends JpaRepository<IpMarDetails,Long> {
}
