package com.hims.entity.repository;

import com.hims.entity.IpConsumableTxn;
import com.hims.projection.NursingCareProcedureProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IpConsumableTxnRepository extends JpaRepository<IpConsumableTxn,Long> {

    @Query("""
            SELECT
                ict.itemId.itemId AS itemId,
                ict.itemName AS itemName,
                ict.quantity AS qty,
                ipt.procedureTxnId AS procedureTxnId,
                ipt.procedureName AS procedureName,
                ict.usageDatetime AS dateTime,
                ict.usedBy AS usedBy,
                ict.batchNo AS batchNo,
                ict.expiryDate AS expiryDate,
                ict.remarks AS remark
            FROM IpConsumableTxn ict
            LEFT JOIN ict.procedureTxnId ipt
            WHERE ict.inpatientId.inpatientId = :inpatientId
            ORDER BY ict.usageDatetime DESC, ict.consumableTxnId DESC
            """)
    List<NursingCareProcedureProjection> getNursingCareProcedureByInpatientId(@Param("inpatientId") Long inpatientId);
}
