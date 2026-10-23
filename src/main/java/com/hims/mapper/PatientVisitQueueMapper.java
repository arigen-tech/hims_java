package com.hims.mapper;

import com.hims.projection.PatientVisitQueueProjection;
import com.hims.response.PatientVisitQueueResponse;
import com.hims.utils.HMISUtil;
import org.springframework.stereotype.Component;


@Component
public class PatientVisitQueueMapper {

    public PatientVisitQueueResponse mapToResponse(PatientVisitQueueProjection projection) {

        return PatientVisitQueueResponse.builder()
                .visitId(projection.getVisitId())
                .patientId(projection.getPatientId())
                .appointmentStartTime(projection.getAppointmentStartTime())
                .appointmentEndTime(projection.getAppointmentEndTime())
                .visitStatus(projection.getVisitStatus())
                .patientName(projection.getPatientName())
                .age(projection.getAge())
                .gender(projection.getGender())
                .mobileNumber(HMISUtil.getMaskedMobileNumber(projection.getMobileNumber()))
                .build();
    }
}
