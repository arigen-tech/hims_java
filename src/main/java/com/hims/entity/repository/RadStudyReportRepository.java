package com.hims.entity.repository;

import com.hims.entity.RadStudyReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RadStudyReportRepository extends JpaRepository<RadStudyReport,Long> {
    Optional<RadStudyReport> findTopByRadOrderDt_IdOrderByRadStudyReportIdDesc(Long radOrderDtId);
}
