package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ip_intake_output_entry")
public class IpIntakeOutputEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "io_entry_id")
    private Long ioEntryId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inpatient_id", nullable = false, foreignKey = @ForeignKey(name = "ip_intake_output_entry_inpatient_id_fkey"))
    private Inpatient inpatient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false, foreignKey = @ForeignKey(name = "ip_intake_output_entry_patient_id_fkey"))
    private Patient patient;


    @Column(name = "io_type", nullable = false, length = 1)
    private String ioType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intake_type_id", foreignKey = @ForeignKey(name = "ip_intake_output_entry_intake_type_id_fkey"))
    private MasIntakeType intakeType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intake_item_id", foreignKey = @ForeignKey(name = "ip_intake_output_entry_intake_item_id_fkey"))
    private MasIntakeItem intakeItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "output_type_id", foreignKey = @ForeignKey(name = "ip_intake_output_entry_output_type_id_fkey"))
    private MasOutputType outputType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", foreignKey = @ForeignKey(name = "ip_intake_output_entry_route_id_fkey"))
    private MasRoute route;

    @Column(name = "quantity", precision = 8, scale = 2)
    private BigDecimal quantity;

    @Column(name = "unit", length = 10)
    private String unit;

    @Column(name = "observation_datetime", nullable = false)
    private LocalDateTime observationDatetime;

    @Column(name = "shift", length = 20)
    private String shift;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "last_updated_by", length = 200)
    private String lastUpdatedBy;

}