package com.hims.entity.repository;

import com.hims.entity.IpDocument;
import com.hims.projection.DocumentProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IpDocumentRepository extends JpaRepository<IpDocument,Long> {
    @Query("""
        SELECT
            d.documentType AS documentType,
            d.documentNotes AS documentNotes,
            d.fileName AS fileName,
            d.filePath AS filePath
        FROM IpDocument d
        WHERE d.inpatient.inpatientId = :inpatientId
        """)
    List<DocumentProjection> findDocumentsByInpatientId(@Param("inpatientId") Long inpatientId);
}
