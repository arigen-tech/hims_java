package com.hims.entity.repository;

import com.hims.entity.LabResultAmendAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface LabResultAmendAuditRepository extends JpaRepository<LabResultAmendAudit,Long>, JpaSpecificationExecutor<LabResultAmendAudit> {


}
