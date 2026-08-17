package com.hims.entity.repository;

import com.hims.entity.IpNokDetails;
import com.hims.projection.NokDetailsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IpNokDetailsRepository extends JpaRepository<IpNokDetails,Long> {
    @Query("""
        SELECT
            n.nokName AS nokName,
            r.relationName AS relationName,
            n.contactNo AS contactNo,
            n.addressLine AS addressLine,
            n.isPrimary AS isPrimary
        FROM IpNokDetails n
        LEFT JOIN n.nokRelation r
        WHERE n.inpatient.inpatientId = :inpatientId
        ORDER BY n.isPrimary DESC, n.nokId DESC
        LIMIT 1
        """)
    Optional<NokDetailsProjection> findNokDetailsByInpatientId(@Param("inpatientId") Long inpatientId);
}
