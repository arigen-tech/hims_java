package com.hims.mapper;

import com.hims.entity.Visit;
import com.hims.response.OpdVisitResponseDTO;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Component
public class VisitMapper {
    public OpdVisitResponseDTO mapToDTO(Visit visit) {

        return OpdVisitResponseDTO.builder()
                .id(visit.getId())
                .tokenNo(visit.getTokenNo())
                .visitDate(visit.getVisitDate())
                .visitStatus(visit.getVisitStatus())
                .priority(visit.getPriority())

                .patientId(visit.getPatient() != null ?
                        visit.getPatient().getId() : null)
                .patientName(visit.getPatient() != null ?
                        visit.getPatient().getFullName() : null)

                .doctorId(visit.getDoctor() != null ?
                        visit.getDoctor().getUserId() : null)
                .doctorName(visit.getDoctorName())

                .departmentId(visit.getDepartment() != null ?
                        visit.getDepartment().getId() : null)
                .departmentName(visit.getDepartment() != null ?
                        visit.getDepartment().getDepartmentName() : null)

                .hospitalId(visit.getHospital() != null ?
                        visit.getHospital().getId() : null)
                .hospitalName(visit.getHospital() != null ?
                        visit.getHospital().getHospitalName() : null)

                .billingStatus(visit.getBillingStatus())
                .startTime(visit.getStartTime())
                .endTime(visit.getEndTime())
                .visitType(visit.getVisitType())
                .displayPatientStatus(visit.getDisplayPatientStatus())

                .cancelledDateTime(visit.getCancelledDateTime())
                .cancelledBy(visit.getCancelledBy())

                .build();
    }
}
