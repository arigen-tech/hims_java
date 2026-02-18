package com.hims.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;

@Entity
@Table(name = "store_return_m")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreReturnM {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "return_m_id")
    private Long returnMId;



    @Column(name = "returned_by", length = 200)
    private String returnedBy;

    @Column(name = "received_by", length = 200)
    private String receivedBy;

    @Column(name = "return_date", nullable = false)
    private LocalDateTime returnDate;

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

    @ManyToOne
    @JoinColumn(name = "receive_m_id", referencedColumnName = "receive_m_id")
    private StoreIndentReceiveM storeIndentReceiveM;

    @ManyToOne
    @JoinColumn(name = "store_id", referencedColumnName = "department_id")
    private MasDepartment storeDepartment;

    @Column(name = "return_no", length = 150)
    private String returnNo;

}
