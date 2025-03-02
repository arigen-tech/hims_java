package com.hims.dto;

import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTO for {@link com.hims.entity.User}
 */
public record UserDto(Long userId, @Size(max = 12) String aadharNumber, @Size(max = 255) String addressInfo,
                      LocalDate dateOfBirth, @Size(max = 255) String firstName, @Size(max = 255) String username,
                      @Size(max = 20) String phoneNumber, Boolean isVerified, @Size(max = 255) String middleName,
                      @Size(max = 255) String lastName, @Size(max = 100) String nationality,
                      @Size(max = 6) String pinCode, @Size(max = 255) String profilePicture,
                      @Size(max = 20) String verificationMethod, @Size(max = 10) String panNumber,
                      LocalDate passportExpiryDate, @Size(max = 20) String passportNumber,
                      @Size(max = 50) String socialMediaProvider,
                      @Size(max = 255) String socialMediaUserId, String status) implements Serializable {
}
