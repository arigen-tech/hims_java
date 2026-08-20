package com.hims.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "ip_diet_schedule", schema = "public")
@Getter
@Setter
@NoArgsConstructor
public class IpDietSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diet_schedule_id", nullable = false)
    private Long dietScheduleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diet_order_id", nullable = false, foreignKey = @ForeignKey(name = "ip_diet_schedule_diet_order_id_fkey"))
    private IpDietOrder dietOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inpatient_id", nullable = false, foreignKey = @ForeignKey(name = "ip_diet_schedule_inpatient_id_fkey"))
    private Inpatient inpatient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_type_id", nullable = false, foreignKey = @ForeignKey(name = "ip_diet_schedule_meal_type_id_fkey"))
    private MasMealType mealType;

    @Column(name = "diet_date", nullable = false)
    private LocalDate dietDate;

    @Column(name = "serving_time")
    private LocalTime servingTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diet_schedule_status_id", nullable = false, foreignKey = @ForeignKey(name = "ip_diet_schedule_diet_schedule_status_id_fkey"))
    private MasDietScheduleStatus dietScheduleStatus;

    @Column(name = "remarks", length = 300)
    private String remarks;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate = LocalDateTime.now();

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "last_updated_by", length = 200)
    private String lastUpdatedBy;

    @Column(name = "administered_datetime")
    private LocalDateTime administeredDatetime;

    @Column(name = "administered_by", length = 100)
    private String administeredBy;

    @Column(name = "consumed_percentage", precision = 5, scale = 2)
    private BigDecimal consumedPercentage;

    @Column(name = "entry_mode", length = 20)
    private String entryMode;

}