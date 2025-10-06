package com.hims.entity.repository;

import com.hims.entity.DgSampleCollectionDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DgSampleCollectionDetailsRepository extends JpaRepository<DgSampleCollectionDetails,Long> {


    /**
     * Fetch all investigations (SampleCollectionDetails) for a given patientId
     * patientId is available inside DgSampleCollectionHeader (foreign key)
     */
    @Query("SELECT d FROM DgSampleCollectionDetails d " +
            "JOIN d.sampleCollectionHeaderId h " +
            "WHERE h.patient_id.id = :patientId")
    List<DgSampleCollectionDetails> findAllInvestigationsByPatient(@Param("patientId") Long patientId);


    /**
     * Fetch all investigations (SampleCollectionDetails) for a given Order(Header) Id
     */
    List<DgSampleCollectionDetails> findBySampleCollectionHeaderId_SampleCollectionHeaderId(Long headerId);
}
