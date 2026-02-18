package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "mas_employee_language_mapping")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@IdClass(MasEmployeeLanguageMapping.EmployeeLanguageId.class)
public class MasEmployeeLanguageMapping {

    @Id
    @Column(name = "emp_id", nullable = false)
    private Long empId;

    @Id
    @Column(name = "language_id", nullable = false)
    private Long languageId;

    @Column(name = "last_chg_by")
    private Long lastChgBy;

    @Column(name = "last_chg_date")
    @LastModifiedDate
    private Instant lastChgDate;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeeLanguageId implements Serializable {
        private Long empId;
        private Long languageId;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EmployeeLanguageId that = (EmployeeLanguageId) o;
            return Objects.equals(empId, that.empId) &&
                    Objects.equals(languageId, that.languageId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(empId, languageId);
        }
    }

    @PrePersist
    protected void onCreate() {
        if (lastChgDate == null) {
            lastChgDate = Instant.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastChgDate = Instant.now();
    }
}