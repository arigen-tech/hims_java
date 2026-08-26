package com.hims.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ip_diet_order", schema = "public")
@Getter
@Setter
@NoArgsConstructor
public class IpDietOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diet_order_id", nullable = false)
    private Long dietOrderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inpatient_id", nullable = false, foreignKey = @ForeignKey(name = "ip_diet_order_inpatient_id_fkey"))
    private Inpatient inpatient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diet_type_id", nullable = false, foreignKey = @ForeignKey(name = "ip_diet_order_diet_type_id_fkey"))
    private MasDietType dietType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diet_preference_id", foreignKey = @ForeignKey(name = "ip_diet_order_diet_preference_id_fkey"))
    private MasDietPreference dietPreference;

    @Column(name = "from_date", nullable = false)
    private LocalDate fromDate;

    @Column(name = "to_date")
    private LocalDate toDate;

    @Column(name = "special_instruction", length = 500)
    private String specialInstruction;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordered_by")
    private User orderedBy;

    @Column(name = "status", columnDefinition = "char(1) default 'A'")
    private String status = "A";

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate = LocalDateTime.now();

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "last_updated_by", length = 200)
    private String lastUpdatedBy;

    @Column(name = "remark")
    private String remark;
}