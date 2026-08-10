package com.hims.entity.repository;

import com.hims.entity.IpMedicineIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IpMedicineIssueRepository extends JpaRepository<IpMedicineIssue,Long> {
}
