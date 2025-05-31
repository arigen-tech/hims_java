package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
import com.hims.entity.repository.MasItemTypeRepository;
import com.hims.entity.repository.MasStoreGroupRepository;
import com.hims.entity.repository.MasStoreSectionRepository;
import com.hims.entity.repository.UserRepo;
import com.hims.request.MasStoreSectionRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasStateResponse;
import com.hims.response.MasStoreSectionResponse;
import com.hims.response.MasUserTypeResponse;
import com.hims.service.MasStoreSectionService;
import com.hims.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class MasStoreSectionImp implements MasStoreSectionService {
    private static final Logger log = LoggerFactory.getLogger(MasStateServiceImpl.class);
    @Autowired
    private MasItemTypeRepository masItemTypeRepository;
    @Autowired
    private UserRepo userRepo;
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
    public ApiResponse<MasStoreSectionResponse> addMasStoreSection(MasStoreSectionRequest masStoreSectionRequest) {
        Optional<MasItemType> masItemType=masItemTypeRepository.findById(masStoreSectionRequest.getMasItemType());
        if(masItemType.isEmpty()){
            return ResponseUtils.createNotFoundResponse("MasItemType not found", 404);
        }
        MasStoreSection masStoreSection=new MasStoreSection();
        masStoreSection.setSectionCode(masStoreSectionRequest.getSectionCode());
        masStoreSection.setSectionName(masStoreSectionRequest.getSectionName());
        masStoreSection.setStatus("y");
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    },
                    "Current user not found", HttpStatus.UNAUTHORIZED.value());
        }
        masStoreSection.setLastChgBy(String.valueOf(currentUser.getUserId()));
        masStoreSection.setLastChgDate(LocalDate.now());
        masStoreSection.setLastChgTime(getCurrentTimeFormatted());
        masStoreSection.setMasItemType(masItemType.get());
        MasStoreSection masStoreSection1 = masStoreSectionRepository.save(masStoreSection);
        return ResponseUtils.createSuccessResponse(mapToResponse(masStoreSection1 ), new TypeReference<>() {
        });

    }

    @Override
    public ApiResponse<List<MasStoreSectionResponse>> getAllStoreSection(int flag) {
        List<MasStoreSection> masStoreSections;

        if (flag == 1) {
            masStoreSections=masStoreSectionRepository.findByStatusIgnoreCase("Y");
        } else if (flag == 0) {
            masStoreSections=masStoreSectionRepository.findByStatusInIgnoreCase(List.of("Y", "N"));
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid flag value. Use 0 or 1.", 400);
        }

        List<MasStoreSectionResponse> responses = masStoreSections.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
    }

    @Override
    public ApiResponse<MasStoreSectionResponse> findById(Integer id) {
        Optional<MasStoreSection> masStoreSection= masStoreSectionRepository.findById(id);
        if(masStoreSection.isPresent()){
            MasStoreSection masStoreSection1 =  masStoreSection.get();

            return ResponseUtils.createSuccessResponse(mapToResponse( masStoreSection1), new TypeReference<>() {});
        }else{
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "MasStoreSection not found", 404);
        }
    }

    private MasStoreSectionResponse mapToResponse(MasStoreSection masStoreSection) {
        MasStoreSectionResponse response = new  MasStoreSectionResponse();
        response.setSectionId(masStoreSection.getSectionId());
        response.setSectionName(masStoreSection.getSectionName());
        response.setSectionCode(masStoreSection.getSectionCode());
        response.setLastChgBy(masStoreSection.getLastChgBy());
        response.setStatus(masStoreSection.getStatus());
        response.setLastChgTime(masStoreSection.getLastChgTime());
        response.setLastChgBy(masStoreSection.getLastChgBy());
        response.setLastChgDate(masStoreSection.getLastChgDate());
        response.setMasItemType(masStoreSection.getMasItemType().getId());
        return response;
    }
}
