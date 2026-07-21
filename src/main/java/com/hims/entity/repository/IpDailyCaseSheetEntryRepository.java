package com.hims.entity.repository;

import com.hims.entity.IpDailyCaseSheetEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IpDailyCaseSheetEntryRepository extends JpaRepository<IpDailyCaseSheetEntry,Long> {
}
