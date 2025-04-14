package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasUserType;
import com.hims.entity.repository.MasUserTypeRepository;
import com.hims.request.MasUserTypeRequest;
import com.hims.response.ApiResponse;
import com.hims.response.MasUserTypeResponse;
import com.hims.service.MasUserTypeService;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class MasUserTypeServiceImp implements MasUserTypeService {
    @Autowired
    private MasUserTypeRepository masUserTypeRepository;

    @Override
    public ApiResponse<List<MasUserType>> getAllMasUserType(int flag) {
        List<MasUserType> masUserTypes;
        if (flag == 1) {
            masUserTypes= masUserTypeRepository.findByStatusIgnoreCase("Y");
        } else if (flag == 0) {
            masUserTypes = masUserTypeRepository.findByStatusInIgnoreCase(List.of("Y", "N"));
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, "Invalid flag value. Use 0 or 1.", 400);
        }
         List<MasUserType> responses =masUserTypes;


        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
    }



    @Override
    public ApiResponse<MasUserTypeResponse>  addMasUser(MasUserTypeRequest masUserTypeRequest) {
        MasUserType masUserType = new MasUserType();
        if ("Y".equalsIgnoreCase(masUserTypeRequest.getStatus()) || "N".equalsIgnoreCase(masUserTypeRequest.getStatus())) {
            masUserType.setStatus(masUserTypeRequest.getStatus());
            masUserType.setUserTypeName(masUserTypeRequest.getUserTypeName());
            masUserType.setLastChgBy(masUserTypeRequest.getLastChgBy());
            masUserType.setLastChgDate(Instant.now());
            masUserType.setHospitalStaff(masUserTypeRequest.getHospitalStaff());
            masUserType.setMapId(masUserTypeRequest.getMapId());

            return ResponseUtils.createSuccessResponse(convertedToResponse(masUserTypeRepository.save(masUserType)), new TypeReference<>() {
            });
        }
        else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, "Invalid status. Status should be 'Y' or 'N'", 400);
        }

    }


    @Override
    public ApiResponse<MasUserTypeResponse> getByIdMasUserType(Long id) {
        Optional<MasUserType> masUserType= masUserTypeRepository.findById(id);
        if(masUserType.isPresent()){
            MasUserType mas= masUserType.get();

            return ResponseUtils.createSuccessResponse(convertedToResponse(mas), new TypeReference<>() {});
        }else{
            return ResponseUtils.createFailureResponse(null, new TypeReference<MasUserTypeResponse>() {}, "MasUserType not found", 404);
        }
    }

    @Override
    public ApiResponse<MasUserTypeResponse> newAddMasUser(MasUserTypeRequest masUserTypeRequest) {
        MasUserType masUserType = new MasUserType();
        if ("Y".equalsIgnoreCase(masUserTypeRequest.getStatus()) || "N".equalsIgnoreCase(masUserTypeRequest.getStatus())) {
            masUserType.setStatus(masUserTypeRequest.getStatus());
            masUserType.setUserTypeName(masUserTypeRequest.getUserTypeName());
            masUserType.setLastChgBy(masUserTypeRequest.getLastChgBy());
            masUserType.setLastChgDate(Instant.now());
            masUserType.setHospitalStaff(masUserTypeRequest.getHospitalStaff());
            masUserType.setMapId(masUserTypeRequest.getMapId());

            return ResponseUtils.createSuccessResponse(convertedToResponse(masUserTypeRepository.save(masUserType)), new TypeReference<>() {
            });
        }
        else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, "Invalid status. Status should be 'Y' or 'N'", 400);

        }
    }

    @Override
    public ApiResponse<MasUserTypeResponse> updateMasUserType(MasUserType masUserType, Long id) {
        Optional<MasUserType> oldMasUserType=masUserTypeRepository.findById(id);
        if(oldMasUserType.isPresent()){
            MasUserType newMasUserType=  oldMasUserType.get();
            newMasUserType.setUserTypeName(masUserType.getUserTypeName());
            newMasUserType.setLastChgBy(masUserType.getLastChgBy());
            newMasUserType.setLastChgDate(Instant.now());
            newMasUserType.setHospitalStaff(masUserType.getHospitalStaff());
            newMasUserType.setMapId(masUserType.getMapId());
            return ResponseUtils.createSuccessResponse(convertedToResponse(masUserTypeRepository.save(newMasUserType)), new TypeReference<>() {
            });

        }else{
            return ResponseUtils.createFailureResponse(null, new TypeReference<MasUserTypeResponse>() {}, "MasUserType not found", 404);
        }

    }

    @Override
    public ApiResponse<MasUserTypeResponse> updateMasUserTypeStatus(Long id, String status) {
        Optional<MasUserType> masUserType= masUserTypeRepository.findById(id);
        if(masUserType.isPresent()){
            if("y".equalsIgnoreCase(status)||"n".equalsIgnoreCase(status)){
                MasUserType mas= masUserType.get();
                mas.setStatus(status);
                return ResponseUtils.createSuccessResponse(convertedToResponse(mas), new TypeReference<>() {});
            }else{
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                }, "Invalid status. Status should be 'Y' or 'N'", 400);


            }


        }else{
            return ResponseUtils.createFailureResponse(null, new TypeReference<MasUserTypeResponse>() {}, "MasUserType not found", 404);
        }

    }

    private MasUserTypeResponse convertedToResponse(MasUserType masUserType) {
        MasUserTypeResponse response= new MasUserTypeResponse();
        response.setUserTypeId(masUserType.getUserTypeId());
        response.setStatus(masUserType.getStatus());
        response.setUserTypeName(masUserType.getUserTypeName());
        response.setLastChgBy(masUserType.getLastChgBy());
        response.setLastChgDate(masUserType.getLastChgDate());
        response.setHospitalStaff(masUserType.getHospitalStaff());
        response.setMapId(masUserType.getMapId());
        return response;

        }
    }




