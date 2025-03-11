package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasRole;
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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    @Override
    public ApiResponse<List<UserResponse>> getAllDoctorsBySpeciality(Long speciality) {
        List<UserResponse> response=new ArrayList<>();
        Optional<MasRole> doctorRole = masRoleRepository.findByRoleDesc("Doctor");
        if(doctorRole.get()==null){
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            },"No role for Doctor Found",500);
        }

        List<UserDepartment> users = userDepartmentRepository.findByDepartment(masDepartmentRepository.findById(speciality).get());
        for(UserDepartment user:users){
            if(user.getUser().getRoleId()!=null) {
                if (user.getUser().getRoleId().contains(doctorRole.get().getId())) {
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
}
