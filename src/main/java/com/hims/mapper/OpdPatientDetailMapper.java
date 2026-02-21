package com.hims.mapper;


import com.hims.entity.OpdPatientDetail;
import com.hims.response.OpdPatientDetailResponseDTO;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Component
public class OpdPatientDetailMapper {

    public OpdPatientDetailResponseDTO mapToDTO(OpdPatientDetail opd) {

        return OpdPatientDetailResponseDTO.builder()
                .opdPatientDetailsId(opd.getOpdPatientDetailsId())
                .height(opd.getHeight())
                .weight(opd.getWeight())
                .pulse(opd.getPulse())
                .temperature(opd.getTemperature())
                .bmi(opd.getBmi())
                .bpSystolic(opd.getBpSystolic())
                .bpDiastolic(opd.getBpDiastolic())
                .presentComplaints(opd.getPresentComplaints())
                .workingDiag(opd.getWorkingDiag())
                .icdDiag(opd.getIcdDiag())
                .opdDate(opd.getOpdDate())

                .patientId(opd.getPatient() != null ?
                        opd.getPatient().getId() : null)
                .patientName(opd.getPatient() != null ?
                        opd.getPatient().getFullName() : null)

                .visitId(opd.getVisit() != null ?
                        opd.getVisit().getId() : null)

                .departmentId(opd.getDepartment() != null ?
                        opd.getDepartment().getId() : null)
                .departmentName(opd.getDepartment() != null ?
                        opd.getDepartment().getDepartmentName() : null)

                .doctorId(opd.getDoctor() != null ?
                        opd.getDoctor().getUserId() : null)
                .doctorName(opd.getDoctor() != null ?
                        opd.getDoctor().getFirstName() : null)

                .followUpFlag(opd.getFollowUpFlag())
                .followUpDays(opd.getFollowUpDays())
                .followUpDate(opd.getFollowUpDate())

                .admissionFlag(opd.getAdmissionFlag())
                .build();
    }
}
