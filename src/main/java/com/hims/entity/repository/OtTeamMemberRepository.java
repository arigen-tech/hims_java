package com.hims.entity.repository;

import com.hims.entity.OtTeamMember;
import com.hims.projection.OtTeamMemberProjection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OtTeamMemberRepository extends JpaRepository<OtTeamMember, Long> {

    List<OtTeamMember> findByOtBooking_OtBookingId(Long otBookingId);

    @Query(value = """
            SELECT tm.ot_team_member_id AS "otTeamMemberId",
                   tm.ot_booking_id AS "otBookingId",
                   tm.ot_team_role_id AS "otTeamRoleId",
                   role.role_name AS "roleName",
                   usr.user_id AS "userId",
                   concat_ws(' ', nullif(trim(usr.first_name), ''), nullif(trim(usr.middle_name), ''), nullif(trim(usr.last_name), '')) AS "userFullName",
                   tm.is_primary AS "isPrimary",
                   tm.status AS "status",
                   tm.last_chg_by AS "lastChgBy",
                   tm.last_chg_date AS "lastChgDate"
            FROM public.ot_team_member tm
            JOIN public.mas_ot_team_role role ON role.ot_team_role_id = tm.ot_team_role_id
            JOIN public.users usr ON usr.user_id = tm.user_id
            WHERE tm.ot_booking_id = :otBookingId
              AND upper(tm.status) = 'Y'
            ORDER BY tm.ot_team_member_id ASC
            """, nativeQuery = true)
    List<OtTeamMemberProjection> findActiveTeamMembersByBookingId(@Param("otBookingId") Long otBookingId);
}
