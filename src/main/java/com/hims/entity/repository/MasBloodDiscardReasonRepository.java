package com.hims.entity.repository;


import com.hims.entity.MasBloodDiscardReason;
import com.hims.response.MasBloodDiscardReasonRes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MasBloodDiscardReasonRepository
        extends JpaRepository<MasBloodDiscardReason, Long> {

    @Query("""
            SELECT new com.hims.response.MasBloodDiscardReasonRes(
                m.discardReasonId,
                m.discardReasonCode,
                m.discardReasonName,
                m.description
            )
            FROM MasBloodDiscardReason m
            WHERE
                :flag = 0
                OR (
                    :flag = 1
                    AND LOWER(m.status) = 'y'
                )
            ORDER BY m.discardReasonId DESC
            """)
    List<MasBloodDiscardReasonRes> getAllDiscardReasons(
            @Param("flag") Integer flag
    );
}
