package com.hims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "user_applications")
public class UserApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_app_id", nullable = false)
    private Long id;

    @Size(max = 180)
    @Column(name = "user_app_name", length = 180)
    private String userAppName;

    @Column(name = "url", length = Integer.MAX_VALUE)
    private String url;

    @Size(max = 4)
    @Column(name = "status", length = 4)
    private String status;

    @Column(name = "last_chg_by")
    private Long lastChgBy;

    @Column(name = "last_chg_date")
    private Instant lastChgDate;

}
