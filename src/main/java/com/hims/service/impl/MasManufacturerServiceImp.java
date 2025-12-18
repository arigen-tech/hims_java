package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasDistrict;
import com.hims.entity.MasHSN;
import com.hims.entity.MasManufacturer;
import com.hims.entity.User;
import com.hims.entity.repository.MasHsnRepository;
import com.hims.entity.repository.MasManufacturerRepository;
import com.hims.entity.repository.UserRepo;
import com.hims.request.MasManufacturerRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasDistrictResponse;
import com.hims.response.MasHsnResponse;
import com.hims.service.MasManufacturerService;
import com.hims.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@Service
public class MasManufacturerServiceImp implements MasManufacturerService {
    private static final Logger log = LoggerFactory.getLogger(MasGenderServiceImpl.class);
    @Autowired
    UserRepo userRepo;


    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUserName(username);
        if (user == null) {
            log.warn("User not found for username: {}", username);
        }
        return user;
    }
    @Autowired
    private MasManufacturerRepository masManufacturerRepository;

    @Override
    public ApiResponse<List<MasManufacturer>> getAllMasManufacturer(int flag) {
        List<MasManufacturer> masManufacturers;
        if (flag == 1) {
            masManufacturers = masManufacturerRepository.findByStatusIgnoreCaseOrderByManufacturerNameAsc("y");
        } else if (flag == 0) {
            masManufacturers = masManufacturerRepository.findAllByOrderByStatusDescLastUpdatedDtDesc();
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, "Invalid flag value. Use 0 or 1.", 400);
        }

        return ResponseUtils.createSuccessResponse(masManufacturers, new TypeReference<>() {
        });
    }

    @Override
    public ApiResponse<MasManufacturer> findById(Long id) {
        Optional<MasManufacturer> masManufacturer = masManufacturerRepository.findById(id);
        if (masManufacturer.isPresent()) {
            return ResponseUtils.createSuccessResponse(masManufacturer.get(), new TypeReference<>() {
            });
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<MasManufacturer>() {
            }, "MasManufacturer not found", 404);
        }
    }

    @Override
    public ApiResponse<MasManufacturer> addMasManufacturer(MasManufacturerRequest masManufacturerRequest) {
       // try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                        },
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }
            MasManufacturer masManufacturer = new MasManufacturer();
            masManufacturer.setManufacturerName(masManufacturerRequest.getManufacturerName());
            masManufacturer.setEmail(masManufacturerRequest.getEmail());
            masManufacturer.setAddress(masManufacturerRequest.getAddress());
            masManufacturer.setDescription(masManufacturerRequest.getDescription());
            masManufacturer.setContactNumber(masManufacturerRequest.getContactNumber());
            masManufacturer.setLastUpdatedBy( currentUser.getLastName());
            masManufacturer.setStatus("y");
            masManufacturer.setLastUpdatedDt(LocalDateTime.now());
            return ResponseUtils.createSuccessResponse(masManufacturerRepository.save(masManufacturer), new TypeReference<>() {
            });
//        } catch (Exception ex) {
//            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
//                    },
//                    "An unexpected error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
//        }
    }

    @Override
    public ApiResponse<MasManufacturer> changeMasManufacturer(Long id, String status) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }

            if (!"y".equalsIgnoreCase(status) && !"n".equalsIgnoreCase(status)) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Invalid status value. Use 'y' or 'n'.", HttpStatus.BAD_REQUEST.value());
            }

            Optional<MasManufacturer> masManufacturer= masManufacturerRepository.findById(id);
            if (masManufacturer.isEmpty()) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "MasManugacturer not found", HttpStatus.NOT_FOUND.value());
            }

            MasManufacturer masManufacturer1 = masManufacturer.get();
            masManufacturer1.setStatus(status.toLowerCase());
            masManufacturer1.setLastUpdatedBy(currentUser.getUsername());
            masManufacturer1.setLastUpdatedDt(LocalDateTime.now());
            MasManufacturer updatedEntity = masManufacturerRepository.save(masManufacturer1); // Save the change


            return ResponseUtils.createSuccessResponse(updatedEntity, new TypeReference<>() {}
            );

        } catch (Exception ex) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<MasManufacturer> update(Long id, MasManufacturerRequest request) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }

            Optional<MasManufacturer> masManufacturer = masManufacturerRepository.findById(id);
            if (masManufacturer.isEmpty()) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<MasManufacturer>() {},
                        "MasManufacturer not found", HttpStatus.NOT_FOUND.value());
            }

            MasManufacturer masManufacturer1 = masManufacturer.get();

           masManufacturer1.setManufacturerName(request.getManufacturerName());
           masManufacturer1.setEmail(request.getEmail());
           masManufacturer1.setAddress(request.getAddress());
           masManufacturer1.setDescription(request.getDescription());
           masManufacturer1.setLastUpdatedDt(LocalDateTime.now());
           masManufacturer1.setLastUpdatedBy(currentUser.getLastName());
           masManufacturer1.setContactNumber(request.getContactNumber());
            MasManufacturer updatedEntity = masManufacturerRepository.save(masManufacturer1);

            return ResponseUtils.createSuccessResponse(updatedEntity, new TypeReference<>() {});
        } catch (Exception e) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }


}

