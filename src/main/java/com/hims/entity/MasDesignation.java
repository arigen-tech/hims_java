package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mas_designation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasDesignation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "designation_id")
    private Long designationId;

    @Column(name = "designation_name", length = 100, nullable = false)
    private String designationName;

     @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_type_id")
    private MasUserType userTypeId;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "last_updated_by", length = 200)
    private String lastUpdatedBy;
}
