package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasItemClass;
import com.hims.entity.MasStoreSection;
import com.hims.entity.User;
import com.hims.entity.repository.MasItemClassRepository;
import com.hims.entity.repository.MasStoreSectionRepository;
import com.hims.entity.repository.UserRepo;
import com.hims.request.MasItemClassRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasItemClassResponse;
import com.hims.service.MasItemClassService;
import com.hims.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MasItemClassServiceImp implements MasItemClassService {
    private static final Logger log = LoggerFactory.getLogger(MasStateServiceImpl.class);
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private MasItemClassRepository masItemClassRepository;
    @Autowired
    private MasStoreSectionRepository masStoreSectionRepository;
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUserName(username);
        if (user == null) {
            log.warn("User not found for username: {}", username);
        }
        return user;
    }

    private String getCurrentTimeFormatted() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    @Override
    public ApiResponse<MasItemClassResponse> addMasItemClass(MasItemClassRequest masItemClassRequest) {
        Optional<MasStoreSection> masStoreSection=masStoreSectionRepository.findById(masItemClassRequest.getSectionId());
        if( masStoreSection.isEmpty()){
            return ResponseUtils.createNotFoundResponse("MasStoreSection not found", 404);
        }
        MasItemClass masItemClass=new MasItemClass();
        masItemClass.setMasStoreSection(masStoreSection.get());
        masItemClass.setItemClassCode(masItemClassRequest.getItemClassCode());
        masItemClass.setItemClassName(masItemClassRequest.getItemClassName());
        masItemClass.setStatus("y");
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    },
                    "Current user not found", HttpStatus.UNAUTHORIZED.value());
        }
        masItemClass.setLastChgBy(String.valueOf(currentUser.getUserId()));
        masItemClass.setLastChgDate(LocalDate.now());
        masItemClass.setLastChgTime(getCurrentTimeFormatted());

        MasItemClass masItemClass1 = masItemClassRepository.save(masItemClass);
        return ResponseUtils.createSuccessResponse(mapToResponse( masItemClass1), new TypeReference<>() {
        });


    }

    @Override
    public ApiResponse<List<MasItemClassResponse>> getAllMasItemClass(int flag) {
        List<MasItemClass> masItemClass;

        if (flag == 1) {
            masItemClass = masItemClassRepository.findByStatusIgnoreCaseOrderByLastChgDateDescLastChgTimeDesc("y");
        } else if (flag == 0) {
            masItemClass = masItemClassRepository.findByStatusInIgnoreCaseOrderByLastChgDateDescLastChgTimeDesc(List.of("y", "n"));
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, "Invalid flag value. Use 0 or 1.", 400);
        }

        List<MasItemClassResponse> responses = masItemClass.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {
        });
    }


    @Override
    public ApiResponse<MasItemClassResponse> findById(Integer id) {
        Optional<MasItemClass> masItemClass =masItemClassRepository.findById(id);
        if (masItemClass .isPresent()) {
            MasItemClass masItemClass1 = masItemClass .get();

            return ResponseUtils.createSuccessResponse(mapToResponse(masItemClass1), new TypeReference<>() {
            });
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, "MasITemClass not found", 404);
        }
    }

    @Override
    public ApiResponse<MasItemClassResponse> changeMasItemClassStatus(int id, String status) {
        Optional<MasItemClass> masItemClass = masItemClassRepository.findById(id);
        if (masItemClass.isPresent()) {
            MasItemClass masItemClass1 =  masItemClass.get();
            if ("y".equals(status) || "n".equals(status)) {
                masItemClass1.setStatus(status);
                User currentUser = getCurrentUser();
                if (currentUser == null) {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                            },
                            "Current user not found", HttpStatus.UNAUTHORIZED.value());
                }
                masItemClass1.setLastChgBy(String.valueOf(currentUser.getUserId()));
                masItemClass1.setLastChgDate(LocalDate.now());

                return ResponseUtils.createSuccessResponse(mapToResponse( masItemClassRepository.save(masItemClass1)), new TypeReference<>() {
                });

            } else {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                }, "Invalid status. Status should be 'y' or 'n'", 400);
            }

        } else {
            return ResponseUtils.createNotFoundResponse("MasItemClass not found", 404);
        }
    }

    @Override
    public ApiResponse<MasItemClassResponse> updateMasItemClass(int id, MasItemClassRequest masItemClassdRequest) {
        Optional<MasItemClass> masItemClass = masItemClassRepository.findById(id);
        if (masItemClass.isPresent()) {
            MasItemClass masItemClass1 = masItemClass.get();
            masItemClass1.setItemClassCode(masItemClassdRequest.getItemClassCode());
            masItemClass1.setItemClassName(masItemClassdRequest.getItemClassName());
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                        },
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }
            masItemClass1.setLastChgBy(String.valueOf(currentUser.getUserId()));
            masItemClass1.setLastChgDate(LocalDate.now());
            masItemClass1.setLastChgTime(getCurrentTimeFormatted());
            masItemClass1.setStatus(masItemClass.get().getStatus());
            if (masItemClassdRequest.getSectionId() != null) {
                Optional<MasStoreSection> masStoreSection = masStoreSectionRepository.findById(masItemClassdRequest.getSectionId());
                if (masStoreSection.isPresent()) {
                    masItemClass1.setMasStoreSection(masStoreSection.get());
                } else {
                    return ResponseUtils.createNotFoundResponse("MasStoreSection not found", 404);
                }
            }
            return ResponseUtils.createSuccessResponse(mapToResponse(masItemClassRepository.save(masItemClass1 )), new TypeReference<>() {
            });


        }else{
            return ResponseUtils.createNotFoundResponse("MasItemClass not found", 404);
        }

    }


    @Override
    public ApiResponse<List<MasItemClassResponse>> getAllBySectionId(int id) {
        List<MasItemClass> masItemClass = masItemClassRepository.findByMasStoreSectionSectionId(id);

        List<MasItemClassResponse> responses = masItemClass.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {
        });

    }

    private MasItemClassResponse mapToResponse(MasItemClass masItemClass) {
        MasItemClassResponse response = new MasItemClassResponse();
        response.setItemClassId(masItemClass.getItemClassId());
        response.setItemClassCode(masItemClass.getItemClassCode());
        response.setItemClassName(masItemClass.getItemClassName());
        response.setStatus(masItemClass.getStatus());
        response.setLastChgBy(masItemClass.getLastChgBy());
        response.setLastChgDate(masItemClass.getLastChgDate());
        response.setLastChgTime(masItemClass.getLastChgTime());

        if (masItemClass.getMasStoreSection() != null) {
            response.setSectionId(masItemClass.getMasStoreSection().getSectionId());
            response.setSectionName(masItemClass.getMasStoreSection().getSectionName());
        }

        return response;
    }
}
