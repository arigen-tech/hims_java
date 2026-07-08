package com.hims.entity.repository;

import com.hims.entity.IpDiagnosisEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IpDiagnosisEntryRepository extends JpaRepository<IpDiagnosisEntry,Long> {
}
