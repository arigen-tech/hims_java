package com.hims.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@Entity
@Table(name = "store_indent_receive_m")
public class StoreIndentReceiveM {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "receive_m_id")
    private Long receiveMId;


    @Column(name = "received_date", nullable = false)
    private LocalDateTime receivedDate;

    @Column(name = "received_by", length = 200)
    private String receivedBy;

    @Column(name = "remarks", length = 300)
    private String remarks;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "last_updated_by", length = 200)
    private String lastUpdatedBy;

    @Column(name = "is_return", length = 2)
    private String isReturn;


    @ManyToOne
    @JoinColumn(name = "received_dept_id", referencedColumnName = "department_id")
    private MasDepartment receivedDepartment;

    @ManyToOne
    @JoinColumn(name = "indent_m_id", referencedColumnName = "indent_m_id")
    private StoreInternalIndentM storeInternalIndent;

    @ManyToOne
    @JoinColumn(name = "store_id", referencedColumnName = "department_id")
    private MasDepartment storeDepartment;


}