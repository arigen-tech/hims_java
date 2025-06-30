package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasRole;
import com.hims.entity.MasStoreItem;
import com.hims.entity.User;
import com.hims.entity.UserDepartment;
import com.hims.entity.repository.MasDepartmentRepository;
import com.hims.entity.repository.MasRoleRepository;
import com.hims.entity.repository.UserDepartmentRepository;
import com.hims.entity.repository.UserRepo;
import com.hims.response.ApiResponse;
import com.hims.response.UserResponse;
import com.hims.service.UserService;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    MasRoleRepository masRoleRepository;

    @Autowired
    UserDepartmentRepository userDepartmentRepository;

    @Autowired
    UserRepo userRepository;

    @Autowired
    MasDepartmentRepository masDepartmentRepository;

//    @Override
//    public ApiResponse<List<UserResponse>> getAllDoctorsBySpeciality(Long speciality) {
//        List<UserResponse> response=new ArrayList<>();
//        Optional<MasRole> doctorRole = masRoleRepository.findByRoleDesc("Doctor");
//        if(doctorRole.get()==null){
//            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
//            },"No role for Doctor Found",500);
//        }
//
//        List<UserDepartment> users = userDepartmentRepository.findByDepartment(masDepartmentRepository.findById(speciality).get());
//        for(UserDepartment user:users){
//            if(user.getUser().getRoleId()!=null) {
//                if (user.getUser().getRoleId().contains(doctorRole.get().getId())) {
//                    UserResponse doctor = new UserResponse();
//                    doctor.setFirstName(user.getUser().getFirstName());
//                    doctor.setMiddleName(user.getUser().getMiddleName());
//                    doctor.setLastName(user.getUser().getLastName());
//                    doctor.setUserId(user.getUser().getUserId());
//                    response.add(doctor);
//                }
//            }
//        }
//        return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});
//    }

    @Override
    public ApiResponse<List<UserResponse>> getAllDoctorsBySpeciality(Long speciality) {
        List<UserResponse> response = new ArrayList<>();
        Optional<MasRole> doctorRole = masRoleRepository.findByRoleDesc("DOCTOR");

        if (doctorRole.isEmpty()) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "No role for Doctor Found", 500);
        }

        List<UserDepartment> users = userDepartmentRepository.findByDepartment(masDepartmentRepository.findById(speciality).orElse(null));

        for (UserDepartment user : users) {
            if (user.getUser().getRoleId() != null) {
                List<Long> roleIds = List.of(user.getUser().getRoleId().split(","))
                        .stream()
                        .map(String::trim)
                        .map(Long::valueOf)
                        .collect(Collectors.toList());

                if (roleIds.contains(doctorRole.get().getId())) {
                    UserResponse doctor = new UserResponse();
                    doctor.setFirstName(user.getUser().getFirstName());
                    doctor.setMiddleName(user.getUser().getMiddleName());
                    doctor.setLastName(user.getUser().getLastName());
                    doctor.setUserId(user.getUser().getUserId());
                    response.add(doctor);
                }
            }
        }
        return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});
    }


    @Override
    public ApiResponse<List<UserResponse>> getAllUsers(int flag) {
        List<User> users;

        if (flag == 1) {
            // Active users only (status = 'Y')
            users = userRepository.findByStatusIgnoreCase("Y");
        } else if (flag == 0) {
            // All users (active and inactive)
            users = userRepository.findByStatusInIgnoreCase(List.of("Y", "N"));
        } else {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Invalid flag value. Use 0 or 1.", 400);
        }

        List<UserResponse> responses = users.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());

        return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});
    }

    @Override
    public ApiResponse<UserResponse> findByUser(String user) {
        try{
            Optional<User> user1= Optional.ofNullable(userRepository.findByUserName(user));
            if (user1.isPresent()) {
                User user2= user1.get();

                return ResponseUtils.createSuccessResponse(convertToUserResponse (user2), new TypeReference<>() {
                });
            } else {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
                }, "User not found", 404);
            }}catch(Exception ex){
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "An unexpected error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }


    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setUserId(user.getUserId());
        response.setFirstName(user.getFirstName());
        response.setMiddleName(user.getMiddleName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        response.setUserName(user.getUsername());
        response.setStatus(user.getStatus());
        response.setMobileNo(user.getMobileNo());
        response.setRoleId(user.getRoleId());
       response.setUserType(user.getUserType() != null ? user.getUserType() : null);
//        response.setVerified(user.getIsVerified());
        return response;
    }

}
