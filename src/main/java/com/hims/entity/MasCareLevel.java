package com.hims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "mas_care_level")
public class MasCareLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "care_id")
    private Long careId;

    @Column(name = "care_level_name",nullable = false,length = 50)
    private String careLevelName;

    @Column(name = "description",length = 200)
    private String description;

    @Column(name = "status",length = 1)
    private String status;


    @UpdateTimestamp
    @Column(name = "last_update_date")
    private LocalDate lastUpdateDate;

    @Column(name = "created_by",length = 200)
    private String createdBy;

    @Column(name = "updated_by",length = 200)
    private String updatedBy;

}
