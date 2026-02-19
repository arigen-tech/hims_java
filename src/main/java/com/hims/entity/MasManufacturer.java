package com.hims.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "mas_manufacturer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MasManufacturer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "manufacturer_id",nullable = false,unique = true)
    private Long manufacturerId;

    @Column(name = "manufacturer_name", length = 150)
    private String manufacturerName;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "address", columnDefinition = "text")
    private String address;

    @Column(name = "contact_number", length = 20)
    private String contactNumber;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "last_updated_by", length = 150)
    private String lastUpdatedBy;

    @Column(name = "last_updated_dt")
    private LocalDateTime lastUpdatedDt;
}
