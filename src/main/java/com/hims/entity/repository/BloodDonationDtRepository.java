package com.hims.entity.repository;

import com.hims.entity.BloodDonationDt;
import com.hims.entity.BloodDonationHdr;
import com.hims.response.BloodInventoryAvailableStockResponse;
import com.hims.response.BloodInventoryStockDetailResponse;
import com.hims.response.BloodInventoryStockSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BloodDonationDtRepository extends JpaRepository<BloodDonationDt,Long> {



    List<BloodDonationDt> findByDonationHdIdDonationId(BloodDonationHdr hdr);

    List<BloodDonationDt> findByDonationHdId(BloodDonationHdr hdr);

    @Query("""
        SELECT new com.hims.response.BloodInventoryStockDetailResponse(
            dt.unitNo,
            hd.bagNumber,
            mbc.componentCode,
            mbg.bloodGroupCode,
            dt.expiryDate,
            mbis.statusCode,
            dt.volumeMl
        )
        FROM BloodDonationDt dt

        JOIN BloodDonationHdr hd
            ON dt.donationHdId.donationId = hd.donationId

        JOIN MasBloodComponent mbc
            ON dt.componentId.componentId = mbc.componentId

        JOIN BloodComponentInventory bci
            ON dt.donationDtId = bci.donationDtId.donationDtId

        JOIN MasBloodInventoryStatus mbis
            ON bci.inventoryStatus.inventoryStatusId = mbis.inventoryStatusId

        JOIN MasBloodGroup mbg
            ON bci.bloodGroupId.bloodGroupId = mbg.bloodGroupId

        WHERE
            (:bloodGroupId IS NULL
                OR bci.bloodGroupId.bloodGroupId = :bloodGroupId)

        AND (:componentId IS NULL
                OR dt.componentId.componentId = :componentId)

        AND (:inventoryStatusId IS NULL
                OR bci.inventoryStatus.inventoryStatusId = :inventoryStatusId)

        AND (:fromDate IS NULL
                OR dt.expiryDate >= :fromDate)

        AND (:toDate IS NULL
                OR dt.expiryDate <= :toDate)

        ORDER BY dt.expiryDate ASC
    """)
    Page<BloodInventoryStockDetailResponse> getBloodInventoryStockDetail(
            @Param("bloodGroupId") Long bloodGroupId,
            @Param("componentId") Long componentId,
            @Param("inventoryStatusId") Long inventoryStatusId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable
    );

    @Query("""
        SELECT new com.hims.response.BloodInventoryAvailableStockResponse(
            bci.inventoryId,
            dt.unitNo,
            hd.bagNumber,
            mbc.componentCode,
            mbg.bloodGroupCode,
            dt.expiryDate,
            mbis.statusCode,
            dt.volumeMl,
            CASE
                WHEN dt.expiryDate < CURRENT_DATE THEN true
                ELSE false
            END
        )
        FROM BloodDonationDt dt

        JOIN BloodDonationHdr hd
            ON dt.donationHdId.donationId = hd.donationId

        JOIN MasBloodComponent mbc
            ON dt.componentId.componentId = mbc.componentId

        JOIN BloodComponentInventory bci
            ON dt.donationDtId = bci.donationDtId.donationDtId

        JOIN MasBloodInventoryStatus mbis
            ON bci.inventoryStatus.inventoryStatusId = mbis.inventoryStatusId

        JOIN MasBloodGroup mbg
            ON bci.bloodGroupId.bloodGroupId = mbg.bloodGroupId

        WHERE
            (:bloodGroupId IS NULL
                OR bci.bloodGroupId.bloodGroupId = :bloodGroupId)

        AND (:componentId IS NULL
                OR dt.componentId.componentId = :componentId)

        AND (:inventoryStatusId IS NULL
                OR bci.inventoryStatus.inventoryStatusId = :inventoryStatusId)

        AND (:fromDate IS NULL
                OR dt.expiryDate >= :fromDate)

        AND (:toDate IS NULL
                OR dt.expiryDate <= :toDate)

        ORDER BY dt.expiryDate ASC
    """)
    List<BloodInventoryAvailableStockResponse> getAvailableInventoryStocks(
            @Param("bloodGroupId") Long bloodGroupId,
            @Param("componentId") Long componentId,
            @Param("inventoryStatusId") Long inventoryStatusId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );


    // =====================================================
    // 2. BLOOD INVENTORY STOCK SUMMARY
    // =====================================================

    @Query("""
    SELECT new com.hims.response.BloodInventoryStockSummaryResponse(
        mbg.bloodGroupName,
        mbc.componentCode,

        COUNT(CASE
            WHEN bci.inventoryStatus.inventoryStatusId = 1 THEN 1
        END),

        COUNT(CASE
            WHEN bci.inventoryStatus.inventoryStatusId = 3 THEN 1
        END),

        COUNT(CASE
            WHEN bci.inventoryStatus.inventoryStatusId = 2 THEN 1
        END),

        COUNT(CASE
            WHEN bci.inventoryStatus.inventoryStatusId = 5 THEN 1
        END)
    )
    FROM BloodDonationDt dt

    JOIN MasBloodComponent mbc
        ON dt.componentId.componentId = mbc.componentId

    JOIN BloodComponentInventory bci
        ON dt.donationDtId = bci.donationDtId.donationDtId

    JOIN MasBloodGroup mbg
        ON bci.bloodGroupId.bloodGroupId = mbg.bloodGroupId

    WHERE
        (:bloodGroupId IS NULL
            OR bci.bloodGroupId.bloodGroupId = :bloodGroupId)

    AND (:componentId IS NULL
            OR dt.componentId.componentId = :componentId)

    AND (:inventoryStatusId IS NULL
            OR bci.inventoryStatus.inventoryStatusId = :inventoryStatusId)

    AND (:fromDate IS NULL
            OR dt.expiryDate >= :fromDate)

    AND (:toDate IS NULL
            OR dt.expiryDate <= :toDate)

    GROUP BY mbg.bloodGroupName, mbc.componentCode

    ORDER BY mbg.bloodGroupName, mbc.componentCode
""")
    Page<BloodInventoryStockSummaryResponse> getBloodInventoryStockSummary(
            @Param("bloodGroupId") Long bloodGroupId,
            @Param("componentId") Long componentId,
            @Param("inventoryStatusId") Long inventoryStatusId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable
    );




//    @Query("""
//        SELECT new com.hims.response.ComponentWiseStockResponse(
//            mbg.bloodGroupName,
//            mbc.componentCode,
//
//            COUNT(CASE WHEN bci.inventoryStatus = 1 THEN 1 END),
//
//            COUNT(CASE WHEN bci.inventoryStatus = 3 THEN 1 END),
//
//            COUNT(CASE WHEN bci.inventoryStatus = 2 THEN 1 END),
//
//            COUNT(CASE WHEN bci.inventoryStatus = 5 THEN 1 END)
//        )
//        FROM BloodComponentInventory bci
//
//        JOIN MasBloodGroup mbg
//            ON bci.bloodGroupId.bloodGroupId = mbg.bloodGroupId
//
//        JOIN MasBloodComponent mbc
//            ON bci.componentId.componentId = mbc.componentId
//
//        WHERE
//            (:bloodGroupId IS NULL
//                OR bci.bloodGroupId.bloodGroupId = :bloodGroupId)
//
//        AND (:componentId IS NULL
//                OR bci.componentId.componentId = :componentId)
//
//        AND (:inventoryStatusId IS NULL
//                OR bci.inventoryStatus.inventoryStatusId = :inventoryStatusId)
//
//        AND (:fromDate IS NULL
//                OR bci.expiryDate >= :fromDate)
//
//        AND (:toDate IS NULL
//                OR bci.expiryDate <= :toDate)
//
//        GROUP BY mbg.bloodGroupName, mbc.componentCode
//
//        ORDER BY mbg.bloodGroupName, mbc.componentCode
//    """)
//    Page<ComponentWiseStockResponse> getComponentWiseStock(
//            @Param("bloodGroupId") Long bloodGroupId,
//            @Param("componentId") Long componentId,
//            @Param("inventoryStatusId") Long inventoryStatusId,
//            @Param("fromDate") LocalDate fromDate,
//            @Param("toDate") LocalDate toDate,
//            Pageable pageable
//    );

}
