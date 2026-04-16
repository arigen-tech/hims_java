package com.hims.entity.repository;

import com.hims.entity.BloodDonationHdr;
import com.hims.projection.PendingForMandatoryTestingProjection;
import com.hims.response.PendingComponentGenerationResponse;
import com.hims.response.PendingForMandatoryTestingResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BloodDonationHdrRepository extends JpaRepository<BloodDonationHdr,Long> {
    @Query("""
    SELECT new com.hims.response.PendingComponentGenerationResponse(
        bdh.donationId,
        bdh.bagNumber,
        bd.donorCode,
        bd.firstName,
        bd.lastName,
        bg.bloodGroupName,
        bdh.donationDatetime,
        ct.collectionTypeName,
        bt.bagTypeName,
        bdh.totalCollectedVolumeMl,
        ds.donationStatusCode
    )
    FROM BloodDonationHdr bdh
    LEFT JOIN bdh.donorId bd
    LEFT JOIN bd.bloodGroup bg
    LEFT JOIN bdh.collectionTypeId ct
    LEFT JOIN bdh.bagTypeId bt
    LEFT JOIN bdh.donationStatusId ds
    WHERE ds.donationStatusId = :donationStatusId
    ORDER BY bdh.donationId DESC
""")
    List<PendingComponentGenerationResponse> pendingComponentGenerationList(@Param("donationStatusId") Long bloodDonationStatusCollected);

    @Query(value = """
    SELECT donor_code FROM blood_donor 
    WHERE donor_code LIKE CONCAT(:prefix, '%') 
    ORDER BY donor_code DESC LIMIT 1 
    """, nativeQuery = true)
    String findLastDonorCodeByPrefix(@Param("prefix") String prefix);

    @Query(value = """
    SELECT bag_number FROM blood_donation_hdr 
    WHERE bag_number LIKE CONCAT(:prefix, '%') 
    ORDER BY bag_number DESC LIMIT 1 
    """, nativeQuery = true)
    String findLastBagNumberPrefix(@Param("prefix") String prefix);


    @Query("""
            SELECT 
                bd.donationId AS donationId,
                d.donorId AS donorId,
                bd.bagNumber AS bagNumber,
                d.donorCode AS donorResNo,
            
                CONCAT(d.firstName, ' ', d.lastName) AS fullName,
            
                bg.bloodGroupCode AS bloodGroup,
                bd.donationDatetime AS collectionDateTime,
                mct.collectionTypeName AS collectionType,
            
                (SELECT COUNT(dt.donationDtId) 
                 FROM BloodDonationDt dt 
                 WHERE dt.donationHdId.donationId = bd.donationId) AS noOfComponent,
            
                mds.donationStatusCode AS currentStatus,
                mbt.bagTypeCode AS bagType,
                bd.componentGenerationDatetime AS componentGenerationDateTime

            FROM BloodDonationHdr bd
            LEFT JOIN bd.donorId d
            LEFT JOIN d.bloodGroup bg
            LEFT JOIN bd.collectionTypeId mct
            LEFT JOIN bd.bagTypeId mbt
            LEFT JOIN bd.donationStatusId mds
            
            WHERE mds.donationStatusId = :bloodDonationStatusComponentGenerated
            """)
    List<PendingForMandatoryTestingProjection> pendingForMandatoryTestingList(
            @Param("bloodDonationStatusComponentGenerated") Long bloodDonationStatusComponentGenerated);}
