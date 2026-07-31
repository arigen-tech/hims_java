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
@Table(name ="lab_order_tracking_status")
public class LabOrderTrackingStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_status_id")
    private Long orderStatusId;

    @Column(name = "order_status_code",unique = true,nullable = false,length = 10)
    private String orderStatusCode;

    @Column(name = "order_status_name",nullable = false,length = 100)
    private String orderStatusName;

    @Column(name = "description",columnDefinition = "text")
    private String description;

    @Column(name = "status",nullable = false,length = 1)
    private String status;

    @Column(name = "created_by",length = 100)
    private String createdBy;

    @Column(name = "updated_by",length = 100)
    private String updatedBy;

    @Column(name = "update_date")
    @UpdateTimestamp
    private LocalDateTime updateDate;
}
