package com.hims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "mas_ward_room_tariff")
public class MasWardRoomTariff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ward_room_tariff_id")
    private Long wardRoomTariffId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward_id", nullable = false)
    private MasWard ward;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private MasRoom room;

    @Column(name = "tariff", precision = 12, scale = 2, nullable = false)
    private BigDecimal tariff;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "status", length = 1)
    private String status;   // 'y' or 'n'

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "last_updated_by", length = 200)
    private String lastUpdatedBy;

    @UpdateTimestamp
    @Column(name = "last_updated_date")
    private LocalDateTime lastUpdatedDate;

    // Helper methods to get IDs without loading proxies
    public Long getWardId() {
        return ward != null ? ward.getWardId() : null;
    }

    public Long getRoomId() {
        return room != null ? room.getRoomId() : null;
    }
}