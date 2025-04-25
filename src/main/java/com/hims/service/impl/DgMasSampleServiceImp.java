package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.DgMasSample;
import com.hims.entity.DgUom;
import com.hims.entity.User;
import com.hims.entity.repository.DgMasSampleRepository;
import com.hims.entity.repository.UserRepo;
import com.hims.request.DgMasSampleRequest;
import com.hims.response.ApiResponse;
import com.hims.response.DgMasSampleResponse;
import com.hims.response.DgUomResponse;
import com.hims.service.DgMasSampleService;
import com.hims.service.DgUomService;
import com.hims.utils.ResponseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DgMasSampleServiceImp implements DgMasSampleService {
    private static final Logger log = LoggerFactory.getLogger(DgMasSampleServiceImp.class);
    @Autowired
    UserRepo userRepo;
    @Autowired
    private DgMasSampleRepository dgMasSampleRepository;

    @Override
    public ApiResponse<DgMasSampleResponse> addDgMasSample(DgMasSampleRequest dgMasSampleRequest) {

        DgMasSample dgMasSample = new DgMasSample();
        dgMasSample.setSampleCode(dgMasSampleRequest.getSampleCode());
        dgMasSample.setSampleDescription(dgMasSampleRequest.getSampleDescription());
        if ("y".equals(dgMasSampleRequest.getStatus()) || "n".equals(dgMasSampleRequest.getStatus())) {
            dgMasSample.setStatus(dgMasSampleRequest.getStatus());
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, "Invalid status. Status should be 'y' or 'n'", 400);
        }
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                    },
                    "Current user not found", HttpStatus.UNAUTHORIZED.value());
        }
        dgMasSample.setLastChgBy(currentUser.getUsername());
        dgMasSample.setLastChgDate(Instant.now());
        return ResponseUtils.createSuccessResponse(convertedToResponse(dgMasSampleRepository.save(dgMasSample)), new TypeReference<>() {
        });
    }

    @Override
    public ApiResponse<DgMasSampleResponse> getByIdDgMas(Long id) {
        Optional<DgMasSample> dgMasSample = dgMasSampleRepository.findById(id);
        if (dgMasSample.isPresent()) {
            DgMasSample newDgMasSample = dgMasSample.get();
            return ResponseUtils.createSuccessResponse(convertedToResponse(newDgMasSample), new TypeReference<>() {
            });
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, "DgMasSample data not found", 404);

        }


    }

    @Override
    public ApiResponse<List<DgMasSampleResponse>> getAllDgMas(int flag) {
        List<DgMasSample> dpUom;
        if(flag==1){
            dpUom=dgMasSampleRepository.findByStatus("y");
        }else if(flag==0){
            dpUom=dgMasSampleRepository.findAll();

        }else{
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid flag value. Use 0 or 1.", 400);
        }
        List<DgMasSampleResponse> responses = dpUom.stream()
                .map(this::convertedToResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
    }

    @Override
    public ApiResponse<DgMasSampleResponse> updateByStatusDgUom(Long id, String status) {
        Optional<DgMasSample> dgUom = dgMasSampleRepository.findById(id);
        if (dgUom.isPresent()) {
            DgMasSample newDgUom = dgUom.get();
            if(status.equals("n")||status.equals("y")){
                newDgUom.setStatus(status);
            }else{
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                }, "Invalid status. Status should be 'y' or 'n'", 400);
            }
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }
            newDgUom.setLastChgBy(currentUser.getUsername());
            return ResponseUtils.createSuccessResponse(convertedToResponse(dgMasSampleRepository.save(newDgUom)), new TypeReference<>() {
            });

        }else{
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "DgMasSample not found", 404);
        }

    }

    @Override
    public ApiResponse<DgMasSampleResponse> updateByIdDgUom(Long id, DgMasSampleRequest dgMasSampleRequest) {
        Optional<DgMasSample> dgUom=dgMasSampleRepository.findById(id);
        if(dgUom.isPresent()){
            DgMasSample newDgMas=dgUom.get();
            if ("y".equals(dgMasSampleRequest.getStatus()) || "n".equals(dgMasSampleRequest.getStatus())) {
                newDgMas.setStatus(dgMasSampleRequest.getStatus());
            } else {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                }, "Invalid status. Status should be 'y' or 'n'", 400);
            }
            newDgMas.setSampleCode(dgMasSampleRequest.getSampleCode());
            newDgMas.setSampleDescription(dgMasSampleRequest.getSampleDescription());
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }
            newDgMas.setLastChgBy(currentUser.getUsername());
            newDgMas.setLastChgDate(Instant.now());
            return ResponseUtils.createSuccessResponse(convertedToResponse(dgMasSampleRepository.save( newDgMas)), new TypeReference<>() {
            });

        }else{
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "DgMasSample data not found", 404);
        }
    }


    private DgMasSampleResponse convertedToResponse(DgMasSample dgMasSample){
        DgMasSampleResponse response=new DgMasSampleResponse();
        response.setId(dgMasSample.getId());
        response.setSampleCode(dgMasSample.getSampleCode());
        response.setSampleDescription(dgMasSample.getSampleDescription());
        response.setStatus(dgMasSample.getStatus());
        response.setLastChgBy(dgMasSample.getLastChgBy());
        response.setLastChgDate(dgMasSample.getLastChgDate());
       return response;

    }
    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUserName(username);
       if (user == null) {
          log.warn("User not found for username: {}", username);
       }
        return user;
    }


}
