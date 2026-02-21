package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mas_blood_test", schema = "public")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasBloodTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "blood_test_id")
    private Long bloodTestId;

    @Column(name = "test_code", length = 30, nullable = false, unique = true)
    private String testCode;

    @Column(name = "test_name", length = 100, nullable = false)
    private String testName;

    @Column(name = "is_mandatory", length = 1)
    private String isMandatory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicable_collection_type_id")
    private MasBloodCollectionType applicableCollectionTypeId;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "created_by", length = 200)
    private String createdBy;
}
