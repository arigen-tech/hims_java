package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mas_intake_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasIntakeItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "intake_item_id")
    private Long intakeItemId;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intake_type_id", nullable = false)
    private MasIntakeType intakeType;

    @Column(name = "intake_item_name", length = 100, nullable = false, unique = true)
    private String intakeItemName;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "last_updated_by", length = 200)
    private String lastUpdatedBy;

}
