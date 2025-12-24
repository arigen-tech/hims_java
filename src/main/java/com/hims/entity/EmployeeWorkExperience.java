package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.Instant;


@Entity
@Table(name = "emp_work_experience")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeWorkExperience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "experience_id")
    private Long experienceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_id", nullable = false)
    private MasEmployee employee;

    @Column(name = "experience_summary", length = 500)
    private String experienceSummary;

    @Column(name = "last_update_date")
    @UpdateTimestamp
    private Instant lastUpdateDate;

    @Column(name = "order_level")
    private Integer orderLevel;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "emp_id", insertable = false, updatable = false)
//    private MasEmployee employee;
}
