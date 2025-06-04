package com.hims.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "mas_service_category")
public class MasServiceCategory {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 20)
    @NotNull
    @Column(name = "service_cate_code", nullable = false, length = 20)
    private String serviceCateCode;

    @Size(max = 255)
    @NotNull
    @Column(name = "service_cat_name", nullable = false)
    private String serviceCatName;

    @Size(max = 20)
    @Column(name = "sac_code", length = 20)
    private String sacCode;

    @Size(max = 1)
    @NotNull
    @Column(name = "status", nullable = false, length = 1)
    private String status;

    @Size(max = 200)
    @Column(name = "last_chg_by", length = 200)
    private String lastChgBy;

    @NotNull
    @Column(name = "last_chg_dt", nullable = false)
    private Instant lastChgDt;

}
