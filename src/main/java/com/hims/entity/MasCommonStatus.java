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
@Entity
@Table(name = "mas_common_status")
public class MasCommonStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "common_status_id")
    private Long commonStatusId;

    @Column(name = "table_name",length = 100,nullable = false)
    private String tableName;

    @Column(name = "entity_name",length = 100,nullable = false)
    private String entityName;

    @Column(name = "column_name",length = 100,nullable = false)
    private String columnName;

    @Column(name = "status_code",length = 10,nullable = false)
    private String statusCode;

    @Column(name = "status_name",length =50,nullable = false)
    private String statusName;

    @Column(name = "status_description",columnDefinition = "text",nullable = false)
    private String statusDesc;

    @Column(name = "remarks",length = 100)
    private String remarks;

    @Column(name = "updated_by",length = 100,nullable = false)
    private String updatedBy;

    @Column(name ="update_date")
    @UpdateTimestamp
    private LocalDateTime updateDate;
}
