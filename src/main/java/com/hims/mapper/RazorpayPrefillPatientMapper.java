package com.hims.mapper;

import com.hims.projection.RazorpayPrefillPatientProjection;
import com.hims.response.RazorpayPrefillPatientResponse;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class RazorpayPrefillPatientMapper {

    public  RazorpayPrefillPatientResponse toResponse(RazorpayPrefillPatientProjection projection) {

        if (projection == null) {
            return null;
        }

        String fullName = Stream.of(
                        projection.getPatientFn(),
                        projection.getPatientMn(),
                        projection.getPatientLn()
                )
                .filter(part -> part != null && !part.isBlank())
                .collect(Collectors.joining(" "));

        return new RazorpayPrefillPatientResponse(
                fullName,
                projection.getEmail(),
                projection.getPhoneNumber()
        );
    }
}