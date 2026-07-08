package com.hims.entity.repository;

import com.hims.entity.IpNokDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IpNokDetailsRepository extends JpaRepository<IpNokDetails,Long> {
}
