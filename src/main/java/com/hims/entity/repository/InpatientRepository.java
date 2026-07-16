package com.hims.entity.repository;

import com.hims.entity.Inpatient;
import lombok.Locked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InpatientRepository extends JpaRepository<Inpatient,Long> {

    @Query(
            value = """
        SELECT admission_no
        FROM inpatient
        WHERE admission_no LIKE :financialYearPattern
        ORDER BY CAST(SPLIT_PART(admission_no, '/', 3) AS INTEGER) DESC
        LIMIT 1
        """,
            nativeQuery = true
    )
    String findLastAdmissionNoByFinancialYear(
            @Param("financialYearPattern") String financialYearPattern
    );
}
