package com.hims.entity.repository;

import com.hims.entity.IpAdverseEvent;
import com.hims.response.IpAdverseEventResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
public interface IpAdverseEventRepository extends JpaRepository<IpAdverseEvent, Long> {
    @Query("SELECT new com.hims.response.IpAdverseEventResponse(" +
            "ae.adverseEventId, " +
            "ae.inpatientId.inpatientId, " +
            "ae.medicationId.itemId, " +
            "msi.nomenclature, " +
            "ae.reaction, " +
            "ae.severity, " +
            "ae.actionTaken, " +
            "ae.reactionDatetime, " +
            "ae.medicationStopped, " +
            "ae.doctorInformed, " +
            "ae.informedDoctorId.userId, " +
            "CONCAT(u.firstName, ' ', COALESCE(u.middleName, ''), ' ', u.lastName), " +
            "ae.patientConditionAfter) " +
            "FROM IpAdverseEvent ae " +
            "LEFT JOIN ae.medicationId msi " +
            "LEFT JOIN ae.informedDoctorId u " +
            "WHERE ae.inpatientId.inpatientId = :inpatientId " +
            "ORDER BY ae.reactionDatetime DESC")
    List<IpAdverseEventResponse> findAdverseReactionDetailsByInpatientId(@Param("inpatientId") Long inpatientId);
}