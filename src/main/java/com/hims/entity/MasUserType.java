package com.hims.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "mas_user_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasUserType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_type_id")
    private Long userTypeId;

    @Column(name = "user_type_name", length = 180)
    private String userTypeName;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "last_chg_by")
    private Long lastChgBy;

    @Column(name = "last_chg_date")
    private Instant lastChgDate;

    @Column(name = "hospital_staff", length = 1)
    private String hospitalStaff;

    @Column(name = "map_id")
    private Long mapId;
}
