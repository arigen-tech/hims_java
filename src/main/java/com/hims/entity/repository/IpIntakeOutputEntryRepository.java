package com.hims.entity.repository;

import com.hims.entity.IpIntakeOutputEntry;
import com.hims.projection.IntakeOutputProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IpIntakeOutputEntryRepository extends JpaRepository<IpIntakeOutputEntry,Long> {
    @Query("""
            SELECT
                io.inpatient.inpatientId AS inpatientId,
                io.ioEntryId AS ioEntryId,
                io.ioType AS ioType,
                it.intakeTypeId AS intakeTypeId,
                it.intakeTypeName AS intakeTypeName,
                ii.intakeItemId AS intakeItemId,
                ii.intakeItemName AS intakeItemName,
                io.observationDatetime AS dateTime,
                io.quantity AS intakeQuantity,
                ot.outputTypeId AS outputTypeId,
                ot.outputTypeName AS outputName
            FROM IpIntakeOutputEntry io
            LEFT JOIN io.intakeType it
            LEFT JOIN io.intakeItem ii
            LEFT JOIN io.outputType ot
            WHERE io.inpatient.inpatientId = :inpatientId
            ORDER BY io.observationDatetime DESC
            """)
    List<IntakeOutputProjection> getIntakeOutputDetails(@Param("inpatientId") Long inpatientId);

}
