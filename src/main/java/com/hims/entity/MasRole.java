package com.hims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "mas_role")
public class MasRole {

    @Id
    @Size(max = 255)
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "created_on")
    private Instant createdOn;

    @Size(max = 1)
    @Column(name = "status", length = 1)
    private String status;

    @Size(max = 255)
    @Column(name = "role_code")
    private String roleCode;

    @Size(max = 255)
    @Column(name = "role_desc")
    private String roleDesc;

    @NotNull
    @Column(name = "updated_on", nullable = false)
    private Instant updatedOn;

}