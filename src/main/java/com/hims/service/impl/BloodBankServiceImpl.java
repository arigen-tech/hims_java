package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.exception.BloodBankException.DonorSaveException;
import com.hims.exception.BloodBankException.ScreeningSaveException;
import com.hims.request.BloodDonorPersonalDetailsRequest;
import com.hims.request.BloodDonorScreeningRequest;
import com.hims.request.DonorRegistrationRequest;
import com.hims.response.ApiResponse;
import com.hims.response.BloodDonorResponse;
import com.hims.service.BloodBankService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class BloodBankServiceImpl implements BloodBankService{
    @Autowired
    private BloodDonorRepository bloodDonorRepository;
    @Autowired
    private MasGenderRepository masGenderRepository;
    @Autowired
    private MasBloodGroupRepository masBloodGroupRepository;
    @Autowired
    private MasBloodDonationTypeRepository masBloodDonationTypeRepository;
    @Autowired
    private MasCountryRepository masCountryRepository;
    @Autowired
    private MasStateRepository masStateRepository;
    @Autowired
    private MasDistrictRepository masDistrictRepository;
    @Autowired
    private AuthUtil authUtil;
    @Autowired
    private BloodDonorScreeningRepository bloodDonorScreeningRepository;


    private String generateDonorCode() {
        String year = String.valueOf(LocalDate.now().getYear());
        String prefix = "DON-" + year + "-";
        String lastCode = bloodDonorRepository.findLastDonorCodeByPrefix(prefix);
        long nextNumber = 1;
        if (lastCode != null) {
            String numericPart = lastCode.substring(prefix.length());
            nextNumber = Long.parseLong(numericPart) + 1;
        }
        return prefix + String.format("%04d", nextNumber);
    }

    @Override
    @Transactional
    public ApiResponse<BloodDonorResponse> registerDonor(DonorRegistrationRequest request)  {
        BloodDonorPersonalDetailsRequest pd =
                request.getBloodDonorPersonalDetailsRequest();
        boolean exists = bloodDonorRepository.existsByMobileNoAndFirstNameAndDateOfBirthAndRelationAndBloodGroup_BloodGroupId(
                                pd.getMobileNo(),
                                pd.getFirstName(),
                                pd.getDateOfBirth(),
                                pd.getRelation(),
                                pd.getBloodGroupId());
        if (exists) {
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Donor already registered with same details",
                    HttpStatus.BAD_REQUEST.value()
            );
        }

        BloodDonor donor = saveDonorDetails(request.getBloodDonorPersonalDetailsRequest());
        BloodDonorScreening screening = saveDonorScreeningDetails(
                request.getBloodDonorScreeningRequest(), donor);
        BloodDonorResponse response = mapToResponse(donor,screening);
        return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});
    }


    @Transactional
    @Override
    public ApiResponse<BloodDonorResponse> updateDonor(Long donorId, DonorRegistrationRequest request) {
        BloodDonor donor = bloodDonorRepository.findById(donorId)
                .orElseThrow(() -> new DonorSaveException("Donor not found"));

        updateDonorDetails(donor, request.getBloodDonorPersonalDetailsRequest());
        BloodDonorScreening screening = saveDonorScreeningDetails(
                request.getBloodDonorScreeningRequest(), donor);

        BloodDonorResponse response = mapToResponse(donor, screening);
        return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});
    }

    public BloodDonor saveDonorDetails(BloodDonorPersonalDetailsRequest personalDetailsRequest){
        try {
            BloodDonor donor = new BloodDonor();
            donor.setDonorCode(generateDonorCode());
            donor.setFirstName(personalDetailsRequest.getFirstName());
            donor.setLastName(personalDetailsRequest.getLastName());
            if (personalDetailsRequest.getGenderId() != null) {
                MasGender gender = masGenderRepository
                        .getReferenceById(personalDetailsRequest.getGenderId());

                donor.setGender(gender);
            }
            donor.setDateOfBirth(personalDetailsRequest.getDateOfBirth());
            donor.setMobileNo(personalDetailsRequest.getMobileNo());
            if (personalDetailsRequest.getBloodGroupId() != null) {
                MasBloodGroup group = masBloodGroupRepository.
                        getReferenceById(personalDetailsRequest.getBloodGroupId());
                donor.setBloodGroup(group);
            }

            if (personalDetailsRequest.getDonationTypeId() != null) {
                MasBloodDonationType type = masBloodDonationTypeRepository
                        .getReferenceById(personalDetailsRequest.getDonationTypeId());
                donor.setDonationType(type);
            }
            donor.setRelation(personalDetailsRequest.getRelation());
            donor.setDonorStatus(personalDetailsRequest.getDonorStatus());
            donor.setCurrentDeferralReason(personalDetailsRequest.getCurrentDeferralReason());
            donor.setDeferralUptoDate(personalDetailsRequest.getDeferralUptoDate());
            donor.setAddressLine1(personalDetailsRequest.getAddressLine1());
            donor.setAddressLine2(personalDetailsRequest.getAddressLine2());

            if (personalDetailsRequest.getCountryId() != null) {
                MasCountry country = masCountryRepository
                        .getReferenceById(personalDetailsRequest.getCountryId());
                donor.setCountry(country);
            }
            if (personalDetailsRequest.getStateId() != null) {
                MasState state = masStateRepository
                        .getReferenceById(personalDetailsRequest.getStateId());
                donor.setState(state);
            }
            if (personalDetailsRequest.getDistrictId() != null) {
                MasDistrict district = masDistrictRepository
                        .getReferenceById(personalDetailsRequest.getDistrictId());
                donor.setDistrict(district);
            }
            donor.setCity(personalDetailsRequest.getCity());
            donor.setPincode(personalDetailsRequest.getPincode());
            donor.setRemarks(personalDetailsRequest.getRemarks());
            donor.setStatus(personalDetailsRequest.getDonorStatus());
            donor.setCreatedDate(LocalDateTime.now());
            donor.setCreatedBy(authUtil.getCurrentUser().getFirstName());

            return bloodDonorRepository.save(donor);
        }catch (Exception ex){
            throw new DonorSaveException("Failed to save donor details", ex);
        }
    }
    public BloodDonorScreening saveDonorScreeningDetails(BloodDonorScreeningRequest donorScreeningRequest,BloodDonor donor){
        try {
            BloodDonorScreening screening = new BloodDonorScreening();
            screening.setDonor(donor);
            screening.setScreeningDate(LocalDate.now());
            screening.setHemoglobin(donorScreeningRequest.getHemoglobin());
            screening.setWeightKg(donorScreeningRequest.getWeightKg());
            screening.setHeightCm(donorScreeningRequest.getHeightCm());
            screening.setBloodPressure(donorScreeningRequest.getBloodPressure());
            screening.setPulseRate(donorScreeningRequest.getPulseRate());
            screening.setTemperature(donorScreeningRequest.getTemperature());
            screening.setScreeningResult(donorScreeningRequest.getScreeningResult());
            screening.setDeferralType(donorScreeningRequest.getDeferralType());
            screening.setDeferralReason(donorScreeningRequest.getDeferralReason());
            screening.setDeferralUptoDate(donorScreeningRequest.getDeferralUptoDate());
            screening.setRemarks(donorScreeningRequest.getRemarks());
            screening.setCreatedDate(LocalDateTime.now());
            screening.setCreatedBy(authUtil.getCurrentUser().getFirstName());
            return bloodDonorScreeningRepository.save(screening);
        }catch (Exception ex){
            throw new ScreeningSaveException("Failed to save screening details", ex);
        }
    }
    private BloodDonorResponse mapToResponse(BloodDonor donor , BloodDonorScreening screening) {
        BloodDonorResponse response = new BloodDonorResponse();
        response.setDonorId(donor.getDonorId());
        response.setDonorCode(donor.getDonorCode());
        response.setFirstName(donor.getFirstName());
        response.setLastName(donor.getLastName());
        response.setMobileNo(donor.getMobileNo());
        return response;
    }

    private void updateDonorDetails(BloodDonor donor, BloodDonorPersonalDetailsRequest pd) {

        donor.setFirstName(pd.getFirstName());
        donor.setLastName(pd.getLastName());
        donor.setMobileNo(pd.getMobileNo());
        donor.setRelation(pd.getRelation());
        donor.setRemarks(pd.getRemarks());

        if (pd.getGenderId() != null) {
            donor.setGender(masGenderRepository.getReferenceById(pd.getGenderId()));
        }

        if (pd.getBloodGroupId() != null) {
            donor.setBloodGroup(masBloodGroupRepository.getReferenceById(pd.getBloodGroupId()));
        }

        bloodDonorRepository.save(donor);
    }

}
