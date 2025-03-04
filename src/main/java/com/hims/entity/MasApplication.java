package com.hims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "mas_application")
public class MasApplication {

    @Id
    @Column(name = "app_id", nullable = false, length = Integer.MAX_VALUE)
    private String appId;

    @Size(max = 200)
    @Column(name = "name", length = 200)
    private String name;

    @Column(name = "parent_id", length = Integer.MAX_VALUE)
    private String parentId;

    @Column(name = "url", length = Integer.MAX_VALUE)
    private String url;

    @ColumnDefault("nextval('mas_application_order_seq')")
    @Column(name = "order_no")
    private Long orderNo;

    @Size(max = 1)
    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "last_chg_date")
    private Instant lastChgDate;

    @Column(name = "app_sequence_no")
    private Long appSequenceNo;

    @OneToMany(mappedBy = "app")
    private Set<TemplateApplication> templateApplications = new LinkedHashSet<>();

}