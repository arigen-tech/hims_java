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
@Table(name = "mas_labresult_amendment_type")
public class MasLabResultAmendmentType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "amendment_type_id")
    private Long amendmentTypeId;

    @Column(name = "amendment_code",length = 30,unique = true,nullable = false)
    private String amendmentTypeCode;

    @Column(name = "amendment_name",length = 100,nullable = false)
    private String amendmentTypeName;

    @Column(name = "description",columnDefinition = "text")
    private String description;

    @Column(name = "status",length = 1,nullable = false)
    private String status;

    @UpdateTimestamp
    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @Column(name = "last_updated_by",length = 200)
    private String lastUpdatedBy;

    @Column(name = "created_by",length = 200)
    private String createdBy;
}
