package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "mas_employee_center_mapping")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@IdClass(MasEmployeeCenterMapping.EmployeeCenterId.class)
public class MasEmployeeCenterMapping {

    @Id
    @Column(name = "emp_id", nullable = false)
    private Long empId;

    @Id
    @Column(name = "center_id", nullable = false)
    private Long centerId;

    @Column(name = "is_primary")
    private Boolean isPrimary;

    @Column(name = "last_update_date")
    @LastModifiedDate
    private Instant lastUpdateDate;

    // Composite ID class
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeeCenterId implements Serializable {
        private Long empId;
        private Long centerId;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EmployeeCenterId that = (EmployeeCenterId) o;
            return Objects.equals(empId, that.empId) &&
                    Objects.equals(centerId, that.centerId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(empId, centerId);
        }
    }

    @PrePersist
    protected void onCreate() {
        if (isPrimary == null) {
            isPrimary = false;
        }
        lastUpdateDate = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdateDate = Instant.now();
    }
}