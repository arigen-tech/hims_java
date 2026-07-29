package com.hims.entity.repository;


import com.hims.entity.TransactionSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionSequenceRepository extends JpaRepository<TransactionSequence, Long> {

    /**
     * Fetches the transaction sequence row and acquires a database row lock.
     * This prevents duplicate sequence generation when multiple users
     * request a transaction number simultaneously.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT ts
            FROM TransactionSequence ts
            WHERE ts.transactionName = :transactionName
              AND ts.hospital.id = :hospitalId
              AND ts.financialYear = :financialYear
            """)
    Optional<TransactionSequence> findForUpdate(
            @Param("transactionName") String transactionName,
            @Param("hospitalId") Long hospitalId,
            @Param("financialYear") String financialYear
    );

}