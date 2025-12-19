package com.hims.entity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mas_tooth_master")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasToothMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tooth_id")
    private Long toothId;

    @Column(name = "tooth_number", nullable = false, length = 5)
    private String toothNumber;

    @Column(name = "tooth_type", nullable = false, length = 20)
    private String toothType;

    @Column(name = "quadrant", nullable = false)
    private Integer quadrant;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "status", length = 1, nullable = false)
    private String status;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "last_updated_by", length = 200)
    private String lastUpdatedBy;
}
