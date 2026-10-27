package com.hims.mapper;

import com.hims.entity.Patient;
import com.hims.response.PatientIdResponse;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class PatientLoginMapper {

    public PatientIdResponse toPatientIdResponse(Patient patient) {
        PatientIdResponse response = new PatientIdResponse();
        response.setPatientId(patient.getId());
        String name = Stream.of(patient.getPatientFn(), patient.getPatientMn(), patient.getPatientLn())
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(part -> !part.isEmpty())
                .collect(Collectors.joining(" "));
        response.setPatientName(name.isEmpty() ? null : name);
        response.setAge(patient.getPatientAge());
        response.setGender(patient.getPatientGender() == null ? null : patient.getPatientGender().getGenderName());
        response.setPatientPhoneNumber(patient.getPatientMobileNumber());
        response.setRelation(patient.getPatientRelation() == null ? null : patient.getPatientRelation().getRelationName());
        return response;
    }
}
