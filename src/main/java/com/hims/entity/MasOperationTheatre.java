package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mas_operation_theatre", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasOperationTheatre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ot_id", nullable = false)
    private Long otId;

    @Column(name = "ot_code", nullable = false, length = 20)
    private String otCode;

    @Column(name = "ot_name", nullable = false, length = 100)
    private String otName;

    @Column(name = "ot_type", nullable = false, length = 30)
    private String otType;

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "status", nullable = false, length = 1)
    private String status;

    @Column(name = "last_chg_by", length = 200)
    private String lastChgBy;

    @Column(name = "last_chg_date", nullable = false)
    private LocalDateTime lastChgDate;
}