package com.hims.entity.repository;

import com.hims.entity.RadStudyReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RadStudyReportRepository extends JpaRepository<RadStudyReport,Long> {
}
