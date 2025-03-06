package com.hims.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@Table(name = "mas_user_department")
public class MasUserDepartment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_department_id", nullable = false)
    private Long userDepartmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private MasDepartment department;

    @Column(name = "las_updated_by")
    private Instant lastUpdatedBy;

    @Column(name = "last_chg_by", length = 255)
    private String lastChangedBy;
}
