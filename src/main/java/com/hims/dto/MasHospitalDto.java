package com.hims.dto;

import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
 * DTO for {@link com.hims.entity.MasHospital}
 */
public record MasHospitalDto(Integer id, @Size(max = 8) String hospitalCode, @Size(max = 30) String hospitalName,
                             @Size(max = 50) String address, @Size(max = 12) String contactNumber,
                             @Size(max = 10) String pinCode, @Size(max = 1) String regCostApplicable,
                             @Size(max = 1) String appCostApplicable,
                             @Size(max = 1) String preConsultationAvailable) implements Serializable {
}
