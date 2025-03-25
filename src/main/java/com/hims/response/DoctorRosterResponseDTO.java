package com.hims.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public class DoctorRosterResponseDTO {
    private Long departmentId;
    private LocalDate fromDate;
    private List<DateEntry> dates;

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public List<DateEntry> getDates() {
        return dates;
    }

    public void setDates(List<DateEntry> dates) {
        this.dates = dates;
    }

    public static class DateEntry {
        private LocalDate dates;
        private String rosterVale;
        private Long doctorId;
        private Long id;

        public LocalDate getDates() {
            return dates;
        }

        public void setDates(LocalDate dates) {
            this.dates = dates;
        }

        public String getRosterVale() {
            return rosterVale;
        }

        public void setRosterVale(String rosterVale) {
            this.rosterVale = rosterVale;
        }

        public Long getDoctorId() {
            return doctorId;
        }

        public void setDoctorId(Long doctorId) {
            this.doctorId = doctorId;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }

}
