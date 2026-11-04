package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ot_team_member", schema = "public", uniqueConstraints = {
        @UniqueConstraint(name = "uk_ot_team_member", columnNames = {"ot_booking_id", "ot_team_role_id", "user_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtTeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ot_team_member_id")
    private Long otTeamMemberId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ot_booking_id", nullable = false)
    private OtBooking otBooking;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ot_team_role_id", nullable = false)
    private MasOtTeamRole otTeamRole;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
    private User user;

    @Column(name = "is_primary", nullable = false, length = 1)
    private String isPrimary;

    @Column(name = "status", nullable = false, length = 1)
    private String status;

    @Column(name = "last_chg_by", length = 200)
    private String lastChgBy;

    @Column(name = "last_chg_date", nullable = false)
    private LocalDateTime lastChgDate;
}
