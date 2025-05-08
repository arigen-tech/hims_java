package com.hims.entity.repository;

import com.hims.entity.MasHospital;
import com.hims.entity.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VisitRepository extends JpaRepository<Visit, Long> {

    @Query("SELECT MAX(v.tokenNo) FROM Visit v " +
                "WHERE v.doctor.userId = :doctorId " +
                "AND v.hospital.id = :hospitalId " +
                "AND v.session.id = :sessionId " +
                "AND FUNCTION('DATE', v.visitDate) = CURRENT_DATE")
    Long findMaxTokenForSessionToday(@Param("doctorId") Long doctorId,
                                     @Param("hospitalId") Long hospitalId,
                                     @Param("sessionId") Long sessionId);

    @Query("SELECT v.tokenNo FROM Visit v " +
            "WHERE v.doctor.userId = :doctorId " +
            "AND v.hospital.id = :hospitalId " +
            "AND v.session.id = :sessionId " +
            "AND FUNCTION('DATE', v.visitDate) = CURRENT_DATE " +
            "ORDER BY v.tokenNo ASC")
    List<Long> findAllTokensForSessionToday(@Param("doctorId") Long doctorId,
                                            @Param("hospitalId") Long hospitalId,
                                            @Param("sessionId") Long sessionId);


    List<Visit> findByHospitalAndPreConsultation(MasHospital hospital,String preConsultation);
}
