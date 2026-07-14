package com.hims.entity.repository;

import com.hims.entity.Inpatient;
import lombok.Locked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface InpatientRepository extends JpaRepository<Inpatient,Long> {
    @Query(value = """
            SELECT admission_no
            FROM inpatient
            WHERE admission_no IS NOT NULL
            ORDER BY inpatient_id DESC
            LIMIT 1
            """, nativeQuery = true)
    String findLastAdmissionNo();
}
