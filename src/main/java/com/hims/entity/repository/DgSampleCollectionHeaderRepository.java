package com.hims.entity.repository;

import com.hims.entity.DgSampleCollectionHeader;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DgSampleCollectionHeaderRepository extends JpaRepository<DgSampleCollectionHeader,Long> {
//    @Modifying
//    @Transactional
//    @Query("UPDATE DgSampleCollectionHeader h SET h.validated = :status WHERE h.sampleCollectionHeaderId = :headerId")
//    int updateOrderStatus(@Param("headerId") Long headerId, @Param("status") String status);
//
//    @Modifying
//    @Transactional
//    @Query("UPDATE DgSampleCollectionHeader h SET h.sampleOrderStatus = :status WHERE h.sampleCollectionHeaderId = :hdId")
//    void updateCollectionStatus(@Param("hdId") Long hdId, @Param("status") String status);


    @Modifying
    @Query("UPDATE DgSampleCollectionHeader h SET h.validated = :status WHERE h.sampleCollectionHeaderId = :headerId")
    void updateValidationStatus(Long headerId, String status);
}
