package com.hims.entity.repository;

import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public interface ReportDao {
    Map<String, Object> getConnectionForReport();

    Map<String, Object> getConnectionForReportMis();
}
