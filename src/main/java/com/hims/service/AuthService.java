package com.hims.service;



import com.hims.entity.User;
import com.hims.jwt.JwtRequest;
import com.hims.jwt.JwtResponce;
import com.hims.jwt.OtpRequest;
import com.hims.request.PasswordChangeReq;
import com.hims.request.ResetPasswordReq;
import com.hims.request.UserCreationReq;
import com.hims.request.UserDetailsReq;
import com.hims.response.ApiResponse;
import com.hims.response.DefaultResponse;
import com.hims.response.RoleInfoResp;
import com.hims.response.UserProfileResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.security.Principal;
import java.util.List;

public interface AuthService {

    ApiResponse<DefaultResponse> createFirstUser(UserCreationReq userCreationReq);

    ApiResponse<JwtResponce> login(JwtRequest request);

//    ApiResponse<JwtResponce> loginWithOtp(OtpRequest request);

    ApiResponse<User> getUser(String userName);


    ApiResponse<UserProfileResponse> getUserForProfile(String userName);

    ApiResponse<List<RoleInfoResp>> getRole(String username);

    ApiResponse<DefaultResponse> createUser(UserDetailsReq userDetailsReq);

    ApiResponse<DefaultResponse> updateUser(UserDetailsReq userDetailsReq);

//    ApiResponse<DefaultResponse> assignUserRole(AssignRoleRequest assignRoleRequest);

    ApiResponse<DefaultResponse> activeInactiveRole(String userName, String roll_id, boolean status);

    ApiResponse<DefaultResponse> activeInactiveUser(String userName, boolean status);

    ApiResponse<List<User>> getAllUser();

    ApiResponse<DefaultResponse> changePassword(PasswordChangeReq request);

    ApiResponse<DefaultResponse> resetPassword(ResetPasswordReq resetPasswordReq);

//    ApiResponse<DefaultResponse> sendOtp(String username);

    ApiResponse<String> getCurrentUser(Principal principal);

    ApiResponse<DefaultResponse> logout(HttpServletRequest request);

    ApiResponse<Long> getActiveUserCount();

}
