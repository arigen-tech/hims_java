package com.hims.entity.repository;

import com.hims.entity.IpIntakeOutputEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IpIntakeOutputEntryRepository extends JpaRepository<IpIntakeOutputEntry,Long> {
}
