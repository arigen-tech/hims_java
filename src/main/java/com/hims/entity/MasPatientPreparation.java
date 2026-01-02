package com.hims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "mas_patient_preparation")
@Entity
public class MasPatientPreparation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "preparation_id")
    private Long preparationId;

    @Column(name = "preparation_code",length = 30,unique = true,nullable = false)
    private String preparationCode;

    @Column(name = "preparation_name",length = 150,nullable = false)
    private String preparationName;

    @Column(name = "instructions",columnDefinition = "text",nullable = false)
    private String instructions;

    @Column(name = "applicable_to",nullable = false)
    private String applicableTo;

    @Column(name = "status",length = 1,nullable = false)
    private String status;

    @UpdateTimestamp
    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @Column(name = "last_updated_by",length = 100)
    private String lastUpdatedBy;

    @Column(name = "created_by",length = 100)
    private String createdBy;

}
