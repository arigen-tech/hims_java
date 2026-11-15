
package com.hims.entity.repository;

import com.hims.entity.OtPacAssessment;
import com.hims.entity.OtBooking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface OtPacAssessmentRepository
        extends JpaRepository<OtPacAssessment, Long> {

 @Lock(LockModeType.PESSIMISTIC_WRITE)
 @Query("SELECT b FROM OtBooking b WHERE b.otBookingId = :bookingId")
 Optional<OtBooking> lockBooking(
         @Param("bookingId") Long bookingId
 );

 @Query(
         value = """
                SELECT *
                FROM public.ot_pac_assessment
                WHERE ot_booking_id = :bookingId
                  AND status = 'Y'
                ORDER BY assessment_no DESC
                LIMIT 1
                """,
         nativeQuery = true
 )
 Optional<OtPacAssessment> findLatestByBookingId(
         @Param("bookingId") Long bookingId
 );

 @Query(
         value = """
                SELECT *
                FROM public.ot_pac_assessment
                WHERE pac_assessment_id = :id
                """,
         nativeQuery = true
 )
 Optional<OtPacAssessment> findNativeById(
         @Param("id") Long id
 );

 @Query(
         value = """
                SELECT COALESCE(MAX(assessment_no), 0) + 1
                FROM public.ot_pac_assessment
                WHERE ot_booking_id = :bookingId
                """,
         nativeQuery = true
 )
 Integer nextAssessmentNo(
         @Param("bookingId") Long bookingId
 );

 @Query(
         value = """
                SELECT user_id
                FROM public.ot_team_member tm
                JOIN public.mas_ot_team_role r
                    ON r.ot_team_role_id = tm.ot_team_role_id
                WHERE tm.ot_booking_id = :bookingId
                  AND UPPER(tm.status) = 'Y'
                  AND UPPER(r.status) = 'Y'
                  AND UPPER(r.role_code) LIKE '%ANAESTHETIST%'
                ORDER BY tm.is_primary DESC, tm.ot_team_member_id
                LIMIT 1
                """,
         nativeQuery = true
 )
 Optional<Long> findAnaesthetistId(
         @Param("bookingId") Long bookingId
 );
}
