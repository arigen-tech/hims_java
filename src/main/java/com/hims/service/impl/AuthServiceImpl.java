package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasEmployee;
import com.hims.entity.MasHospital;
import com.hims.entity.MasUserType;
import com.hims.entity.User;
import com.hims.entity.repository.MasEmployeeRepository;
import com.hims.entity.repository.MasHospitalRepository;
import com.hims.entity.repository.MasUserTypeRepository;
import com.hims.entity.repository.UserRepo;
import com.hims.exception.SDDException;
import com.hims.helperUtil.HelperUtils;
import com.hims.helperUtil.ResponseUtils;
import com.hims.jwt.*;
import com.hims.request.PasswordChangeReq;
import com.hims.request.ResetPasswordReq;
import com.hims.request.UserCreationReq;
import com.hims.request.UserDetailsReq;
import com.hims.response.ApiResponse;
import com.hims.response.DefaultResponse;
import com.hims.response.RoleInfoResp;
import com.hims.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.security.Principal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@Service
public class AuthServiceImpl implements AuthService {


    private final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepo userRepo;

    @Autowired
    private MasHospitalRepository masHospitalRepository;

    @Autowired
    private MasUserTypeRepository masUserTypeRepository;

    @Autowired
    private MasEmployeeRepository masEmployeeRepository;



//    @Autowired
//    private MasRoleRepo masRoleRepo;
//    @Autowired
//    private UserRoleRepo userRoleRepo;
//    @Autowired
//    private MasGenderRepository masGenderRepository;
//    @Autowired
//    private MasStateRepository masStateRepository;
//    @Autowired
//    private MasCountryRepository masCountryRepository;
//    @Autowired
//    private LoginAuditRepo loginAuditRepo;
    @Autowired
    private HttpServletRequest httpServletRequest;
//    @Autowired
//    private JavaMailSender emailSender;
    @Autowired
    private AuthenticationManager manager;
    @Autowired
    private JwtHelper helper;
    @Autowired
    private CustomeUserDetailsImpl userDetailsServiceImpl;
    @Autowired
    private TokenBlacklistService tokenBlacklistService;
    @Autowired
    private RestTemplate restTemplate;
//    @Value("${third.party.otp.api.key}")
//    private String apiKey;
//    @Value("${third.party.otp.send.url}")
//    private String sendOtpUrl;
//    @Value("${third.party.otp.validate.url}")
//    private String validateOtpUrl;




//    public void sendEmail(String toEmail, String subject, String msg) {
//        SimpleMailMessage message = new SimpleMailMessage();
//        try{
//            message.setFrom("arvindrajcs@gmail.com");
//            message.setTo(toEmail);
//            message.setSubject(subject);
//            message.setText(msg);
//            emailSender.send(message);
//        }catch (Exception e){
//            logger.error("An error occurred while sending email", e);
//        }
//    }


    @Transactional(rollbackFor = {Exception.class})
    @Override
    public ApiResponse<DefaultResponse> createFirstUser(UserCreationReq userCreationReq) {
        DefaultResponse defaultResponse = new DefaultResponse();

        if (userRepo.findByUserName(userCreationReq.getEmail()) != null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "USER ALREADY EXISTS WITH THIS EMAIL ID", 400);
        }
        if (userRepo.findByPhoneNumber(userCreationReq.getPhoneNumber()) != null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "USER ALREADY EXISTS WITH THIS MOBILE NO", 400);
        }

        if (userCreationReq.getEmail() == null || userCreationReq.getEmail().isEmpty()) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "EMAIL CANNOT BE BLANK", 400);
        }
        if (userCreationReq.getCurrentPassword() == null || userCreationReq.getCurrentPassword().isEmpty()) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "PASSWORD CANNOT BE BLANK", 400);
        }
        if (userCreationReq.getPhoneNumber() == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "MOBILE NO CANNOT BE BLANK", 400);
        }
        if (userCreationReq.getFirstName() == null || userCreationReq.getFirstName().isEmpty()) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "FIRST NAME CANNOT BE BLANK", 400);
        }
        if (userCreationReq.getLastName() == null || userCreationReq.getLastName().isEmpty()) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "LAST NAME CANNOT BE BLANK", 400);
        }
        if (userCreationReq.getEmployeeId() == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "EMPLOYEE ID CANNOT BE BLANK", 400);
        }
        if (userCreationReq.getHospitalId() == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "HOSPITAL ID CANNOT BE BLANK", 400);
        }
        if (userCreationReq.getUserTypeId() == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "USER TYPE ID CANNOT BE BLANK", 400);
        }

        try {
            MasEmployee employee = masEmployeeRepository.findById(userCreationReq.getEmployeeId()).orElse(null);
            MasHospital hospital = masHospitalRepository.findById(userCreationReq.getHospitalId()).orElse(null);
            MasUserType userType = masUserTypeRepository.findById(userCreationReq.getUserTypeId()).orElse(null);

            if (employee == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "INVALID EMPLOYEE ID", 400);
            }
            if (hospital == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "INVALID HOSPITAL ID", 400);
            }
            if (userType == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "INVALID USER TYPE ID", 400);
            }

            User user = new User();
            user.setUserName(userCreationReq.getUserName() != null ? userCreationReq.getUserName() : userCreationReq.getEmail());
            user.setEmail(userCreationReq.getEmail());
            user.setPhoneNumber(userCreationReq.getPhoneNumber());
            user.setCurrentPassword(passwordEncoder.encode(userCreationReq.getCurrentPassword()));
            user.setFirstName(userCreationReq.getFirstName());
            user.setLastName(userCreationReq.getLastName());
            user.setUserFlag(userCreationReq.getUserFlag());
            user.setEmployee(employee);
            user.setHospital(hospital);
            user.setUserType(userType);
            user.setStatus("y");
            user.setCreatedAt(Instant.now());
            user.setCreatedBy("0");

            userRepo.save(user);

            defaultResponse.setMsg("First User Created Successfully");
            return ResponseUtils.createSuccessResponse(defaultResponse, new TypeReference<DefaultResponse>() {});

        } catch (Exception e) {
            logger.error("An error occurred while creating User", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "An error occurred while creating User", 500);
        }
    }


    @Transactional(rollbackFor = {Exception.class})
    @Override
    public ApiResponse<JwtResponce> login(JwtRequest request) {
        if (!request.isAgent()) {
            request.setRole("101");
        }
        if (request.getUsername() == null || request.getUsername().isEmpty()) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "USERNAME CAN NOT BE BLANK", 400);
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "PASSWORD CAN NOT BE BLANK", 400);
        }
        if (request.getRole() == null || request.getRole().isEmpty()) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "ROLE ID CAN NOT BE BLANK", 400);
        }
        JwtResponce response;
        try {
            String username = "";
            User emailObj = userRepo.findByUserName(request.getUsername());
            if (emailObj == null) {
                User mobObj = userRepo.findByPhoneNumber(request.getUsername());
                if (mobObj != null) {
                    username = mobObj.getUsername();
                } else {
                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "INVALID USERNAME !", 401);
                }
            } else {
                username = request.getUsername();
            }
            User isUser = userRepo.findByUserNameAndStatus(username,"y");
            if (isUser == null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "ACTIVE USER NOT FOUND FROM THIS USERNAME", 400);
            }
//            MasRole obj =masRoleRepo.findByRoleCode(request.getRole());
//            if (obj == null) {
//                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "INVALID ROLL, NOT FOUND IN MAS ROLE", 400);
//            }
//            UserRole roleObj = userRoleRepo.findByUserIdAndRoleAndStatus(isUser,obj, "y");
//            if (roleObj== null) {
//                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},  "USER DON'T HAVE THIS ROLL ID", 400);
//            }
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, request.getPassword());
            try {
                manager.authenticate(authentication);
            } catch (BadCredentialsException e) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},  " Invalid Username or Password  !!", 401);
            }
            UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(username);
            String token = this.helper.generateAccessToken(emailObj);
//          String refreshToken = this.helper.generateRefreshToken(userDetails);
            long expirationTime = helper.getExpirationTime(token);
            response = JwtResponce.builder()
                    .jwtToken(token)
//                  .refreshToken(refreshToken)
//                    .username(userDetails.getUsername() + " or " + roleObj.getUserId().getPhoneNumber())
//                    .role(roleObj.getRole().getRoleDesc())
                    .build();
            saveLoginAudit("password", username, request.getUsername(), request.getRole(), token, expirationTime);
        } catch (Exception e) {
            logger.error("An error occurred while  User login with password", e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "An error occurred while  User login with password", 404);
        }
        return ResponseUtils.createSuccessResponse(response, new TypeReference<JwtResponce>() {
        });
    }



//    @Transactional(rollbackFor = {Exception.class})
//    @Override
//    public ApiResponse<JwtResponce> loginWithOtp(OtpRequest request) {
//        if (!request.isAgent()) {
//            request.setRole("101");
//        }
//        if (request.getUsername() == null || request.getUsername().isEmpty()) {
//            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "USERNAME CAN NOT BE BLANK", 400);
//        }
//        if (request.getOtp() == null || request.getOtp().isEmpty()) {
//            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "PASSWORD CAN NOT BE BLANK", 400);
//        }
//        if (request.getRole() == null || request.getRole().isEmpty()) {
//            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "ROLE ID CAN NOT BE BLANK", 400);
//        }
//        JwtResponce response;
//        try {
//            String username = "";
//            User emailObj = userRepo.findByUsername(request.getUsername());
//            if (emailObj == null) {
//                User mobObj = userRepo.findByPhoneNumber(request.getUsername());
//                if (mobObj != null) {
//                    username = mobObj.getUsername();
//                } else {
//                    return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "INVALID USERNAME !", 400);
//                }
//            } else {
//                username = request.getUsername();
//            }
//            User isUser = userRepo.findByUsernameAndStatus(username,"y");
//            if (isUser == null) {
//                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "ACTIVE USER NOT FOUND FROM THIS USERNAME", 400);
//            }
////            MasRole obj =masRoleRepo.findByRoleCode(request.getRole());
////            if (obj == null) {
////                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "INVALID ROLL, NOT FOUND IN MAS ROLE", 400);
////            }
////            UserRole roleObj = userRoleRepo.findByUserIdAndRoleAndStatus(isUser,obj, "y");
////            if (roleObj== null) {
////                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},  "USER DON'T HAVE THIS ROLL ID", 400);
////            }
//            boolean isOtpValid = validateOtp(username, request.getOtp());
//            if (!isOtpValid) {
//                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},  "INVALID OR EXPIRED OTP!", 401);
//            }
//            UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(username);
//            String token = helper.generateAccessToken(userDetails);
////            String refreshToken = this.helper.generateRefreshToken(userDetails);
//            long expirationTime = helper.getExpirationTime(token);
//            response = JwtResponce.builder()
//                    .jwtToken(token)
////                    .refreshToken(refreshToken)
////                    .username(userDetails.getUsername() + " or " + roleObj.getUserId().getPhoneNumber())
////                    .role(roleObj.getRole().getRoleDesc())
//                    .build();
//            saveLoginAudit("otp",username, request.getUsername(), request.getRole(), token, expirationTime);
//        } catch (Exception e) {
//            logger.error("An error occurred while  User login with OTP", e);
//            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "An error occurred while  User login with OTP", 404);
//        }
//        return ResponseUtils.createSuccessResponse(response, new TypeReference<JwtResponce>() {
//        });
//    }



    @Override
    public ApiResponse<User> getUser(String userName) {
        User isActiveUser;
        if (userName == null || userName.isEmpty()) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "USER NAME CAN NOT BE BLANK", 400);
        }
        User emailObj = userRepo.findByUserName(userName);
        if (emailObj == null ) {
            User mobObj= userRepo.findByPhoneNumber(userName);
            if(mobObj ==null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "INVALID USER", 400);
            }
        }
        isActiveUser = userRepo.findByUserNameAndStatus(userName, "y");
        if (isActiveUser == null) {
            isActiveUser = userRepo.findByPhoneNumberAndStatus(userName,"y");
            if(isActiveUser == null){
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "ACTIVE USER NOT FOUND", 400);
            }
        }
        return ResponseUtils.createSuccessResponse(isActiveUser, new TypeReference<User>() {
        });

    }


    @Override
    public ApiResponse<List<RoleInfoResp>> getRole(String username) {
        if (username == null || username.isEmpty()) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "USER NAME CAN NOT BE BLANK", 400);
        }
        String uName="";
        User emailObj = userRepo.findByUserName(username);
        if (emailObj == null ) {
            User mobObj= userRepo.findByPhoneNumber(username);
            if(mobObj ==null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "INVALID USER", 400);
            }else{
                uName = mobObj.getUsername();
            }
        }else{
            uName = username;
        }

        User isActiveUser = userRepo.findByUserNameAndStatus(uName, "y");
        if (isActiveUser == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "ACTIVE USER NOT FOUND", 400);
        }
        List<RoleInfoResp> obj = new ArrayList<RoleInfoResp>();
//        List<UserRole> roleObj = userRoleRepo.findByUserIdAndStatus(isActiveUser, "y");
//        if (roleObj.size() <= 0) {
//            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "ACTIVE USER HAVE NOT ANY ROLE", 400);
//        }
        try {
//            for (UserRole item : roleObj) {
//                RoleInfoResp rolResp = new RoleInfoResp();
//                MasRole masRole = masRoleRepo.findByRoleCodeAndStatus(item.getRole().getRoleCode(), "y");
//                rolResp.setUserFullName(isActiveUser.getFirstName());
//                rolResp.setUserName(isActiveUser.getUsername() +" or "+isActiveUser.getPhoneNumber());
//                rolResp.setRoleCode(masRole.getRoleCode());
//                rolResp.setRoleDesc(masRole.getRoleDesc());
//                rolResp.setStatus(masRole.getStatus());
//                obj.add(rolResp);
//            }
        } catch (Exception e) {
            logger.error("An error occurred while getting role list", e);
        }
        return ResponseUtils.createSuccessResponse(obj, new TypeReference<List<RoleInfoResp>>() {
        });
    }


    @Transactional(rollbackFor = {Exception.class})
    @Override
    public ApiResponse<DefaultResponse> createUser(UserDetailsReq userDetailsReq) {

        DefaultResponse defaultResponse = new DefaultResponse();
        User isUser = userRepo.findByUserName(userDetailsReq.getEmail());
        if (isUser != null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "USER IS ALREADY EXISTS FROM THIS EMAIL ID", 400);
        }
        User mobObj= userRepo.findByPhoneNumber(userDetailsReq.getGetPhoneNumber());
        if (mobObj != null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "USER IS AVAILABLE FROM THIS MOBILE NO", 400);
        }
        if (userDetailsReq.getEmail() == null || userDetailsReq.getEmail().isEmpty()) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "EMAIL CAN NOT BE BLANK", 400);
        }
        if (userDetailsReq.getGetCurrentPassword() == null || userDetailsReq.getGetCurrentPassword().isEmpty()) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "PASSWORD CAN NOT BE BLANK", 400);
        }
        if (userDetailsReq.getGetCurrentPassword() == null ) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "MOBILE NO CAN NOT BE BLANK", 400);
        }
        if (userDetailsReq.getFirstName() == null || userDetailsReq.getFirstName().isEmpty()) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "FIRST NAME CAN NOT BE BLANK", 400);
        }
        if (userDetailsReq.getMiddleName() == null || userDetailsReq.getMiddleName().isEmpty()) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "MIDDLE NAME CAN NOT BE BLANK", 400);
        }
        if (userDetailsReq.getLastName() == null || userDetailsReq.getLastName().isEmpty()) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "LAST NAME CAN NOT BE BLANK", 400);
        }

        try {
            User obj = new User();

            if(userDetailsReq.getEmail() !=null){
                obj.setUserName(userDetailsReq.getEmail());
            }
            if(userDetailsReq.getGetPhoneNumber() !=null){
                obj.setPhoneNumber(userDetailsReq.getGetPhoneNumber());
            }
            if(userDetailsReq.getGetCurrentPassword() !=null){
                obj.setCurrentPassword(passwordEncoder.encode(userDetailsReq.getGetCurrentPassword()));
            }
            if(userDetailsReq.getGetCurrentPassword() !=null){
                obj.setOldPassword(passwordEncoder.encode(userDetailsReq.getGetCurrentPassword()));
            }
            if(userDetailsReq.getFirstName() !=null){
                obj.setFirstName(userDetailsReq.getFirstName());
            }
            if(userDetailsReq.getMiddleName() !=null){
                obj.setMiddleName(userDetailsReq.getMiddleName());
            }
            if(userDetailsReq.getLastName() !=null){
                obj.setLastName(userDetailsReq.getLastName());
            }
//            if(userDetailsReq.getGender() !=null){
//                obj.setGender(masGenderRepository.findByGenderCode(userDetailsReq.getGender()));
//            }
            if(userDetailsReq.getDob() !=null){
                obj.setDateOfBirth(userDetailsReq.getDob());
            }
            if(userDetailsReq.getNationality() !=null){
                obj.setNationality(userDetailsReq.getNationality());
            }
            if(userDetailsReq.getAddress() !=null){
                obj.setAddressInfo(userDetailsReq.getAddress());
            }
            if(userDetailsReq.getPinCode() !=null){
                obj.setPinCode(userDetailsReq.getPinCode());
            }
//            if(userDetailsReq.getState() !=null){
//                obj.setState(masStateRepository.findById(userDetailsReq.getState()));
//            }
            if(userDetailsReq.getProfilePic() !=null){
                obj.setProfilePicture(userDetailsReq.getProfilePic());
            }
            if(userDetailsReq.getIsVerify() !=null){
                obj.setIsVerified(userDetailsReq.getIsVerify());
            }
            if(userDetailsReq.getVerifyMethod() !=null){
                obj.setVerificationMethod(userDetailsReq.getVerifyMethod());
            }
            if(userDetailsReq.getPanNo() !=null){
                obj.setPanNumber(userDetailsReq.getPanNo());
            }
            if(userDetailsReq.getAadhaarNo() !=null){
                obj.setAadharNumber(userDetailsReq.getAadhaarNo());
            }
            if(userDetailsReq.getPassportNo() !=null){
                obj.setPassportNumber(userDetailsReq.getPassportNo());
            }
//            if(userDetailsReq.getPassportIssueAuthority() !=null){
//                obj.setIssueAuthority(masCountryRepository.findById(Integer.valueOf(userDetailsReq.getPassportIssueAuthority())));
//            }
            if(userDetailsReq.getPassportExpiryDate() !=null){
                obj.setPassportExpiryDate(userDetailsReq.getPassportExpiryDate());
            }
            if(userDetailsReq.getSocialMediaUserId() !=null){
                obj.setSocialMediaUserId(userDetailsReq.getSocialMediaUserId());
            }
            if(userDetailsReq.getSocialMediaProvider() !=null){
                obj.setSocialMediaProvider(userDetailsReq.getSocialMediaProvider());
            }
            obj.setStatus("y");
            obj.setCreatedAt(Instant.now());
            obj.setCreatedBy("1");
            userRepo.save(obj);
            defaultResponse.setMsg("USER Created successfully");
        } catch (Exception e) {
            logger.error("An error occurred while creating User", e);
        }
        return ResponseUtils.createSuccessResponse(defaultResponse, new TypeReference<DefaultResponse>() {
        });
    }


    @Transactional(rollbackFor = {Exception.class})
    @Override
    public ApiResponse<DefaultResponse> updateUser(UserDetailsReq userDetailsReq) {
        DefaultResponse defaultResponse = new DefaultResponse();
        User mobObj= userRepo.findByPhoneNumber(userDetailsReq.getGetPhoneNumber());
        if (mobObj != null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "USER IS AVAILABLE FROM THIS MOBILE NO", 400);
        }

        User isUser = userRepo.findByUserName(userDetailsReq.getEmail());
        if (isUser == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "USER NOT FOUND FROM THIS EMAIL ID", 400);
        }
        try {
            if(userDetailsReq.getGetPhoneNumber() !=null){
                isUser.setPhoneNumber(userDetailsReq.getGetPhoneNumber());
            }
            if(userDetailsReq.getFirstName() !=null){
                isUser.setFirstName(userDetailsReq.getFirstName());
            }
            if(userDetailsReq.getMiddleName() !=null){
                isUser.setMiddleName(userDetailsReq.getMiddleName());
            }
            if(userDetailsReq.getLastName() !=null){
                isUser.setLastName(userDetailsReq.getLastName());
            }
//            if(userDetailsReq.getGender() !=null){
//                isUser.setGender(masGenderRepository.findByGenderCode(userDetailsReq.getGender()));
//            }
            if(userDetailsReq.getDob() !=null){
                isUser.setDateOfBirth(userDetailsReq.getDob());
            }
            if(userDetailsReq.getNationality() !=null){
                isUser.setNationality(userDetailsReq.getNationality());
            }
            if(userDetailsReq.getAddress() !=null){
                isUser.setAddressInfo(userDetailsReq.getAddress());
            }
            if(userDetailsReq.getPinCode() !=null){
                isUser.setPinCode(userDetailsReq.getPinCode());
            }
//            if(userDetailsReq.getState() !=null){
//                isUser.setState(masStateRepository.findById(userDetailsReq.getState()));
//            }
            if(userDetailsReq.getProfilePic() !=null){
                isUser.setProfilePicture(userDetailsReq.getProfilePic());
            }
            if(userDetailsReq.getIsVerify() !=null){
                isUser.setIsVerified(userDetailsReq.getIsVerify());
            }
            if(userDetailsReq.getVerifyMethod() !=null){
                isUser.setVerificationMethod(userDetailsReq.getVerifyMethod());
            }
            if(userDetailsReq.getPanNo() !=null){
                isUser.setPanNumber(userDetailsReq.getPanNo());
            }
            if(userDetailsReq.getAadhaarNo() !=null){
                isUser.setAadharNumber(userDetailsReq.getAadhaarNo());
            }
            if(userDetailsReq.getPassportNo() !=null){
                isUser.setPassportNumber(userDetailsReq.getPassportNo());
            }
//            if(userDetailsReq.getPassportIssueAuthority() !=null){
//                isUser.setIssueAuthority(masCountryRepository.findById(Integer.valueOf(userDetailsReq.getPassportIssueAuthority())));
//            }
            if(userDetailsReq.getPassportExpiryDate() !=null){
                isUser.setPassportExpiryDate(userDetailsReq.getPassportExpiryDate());
            }
            if(userDetailsReq.getSocialMediaUserId() !=null){
                isUser.setSocialMediaUserId(userDetailsReq.getSocialMediaUserId());
            }
            if(userDetailsReq.getSocialMediaProvider() !=null){
                isUser.setSocialMediaProvider(userDetailsReq.getSocialMediaProvider());
            }
            isUser.setLastChangedBy("1");
            isUser.setLastChangeDate(Instant.now());
            userRepo.save(isUser);
            defaultResponse.setMsg("USER Updated successfully");
        } catch (Exception e) {
            logger.error("An error occurred while updating User", e);
        }
        return ResponseUtils.createSuccessResponse(defaultResponse, new TypeReference<DefaultResponse>() {
        });
    }



//    @Transactional(rollbackFor = {Exception.class})
//    @Override
//    public ApiResponse<DefaultResponse> assignUserRole(AssignRoleRequest assignRoleRequest) {
//
//        DefaultResponse defaultResponse = new DefaultResponse();
//        List<String> roleMessages = new ArrayList<>();
//
//        if (assignRoleRequest.getUsername() == null || assignRoleRequest.getUsername().isEmpty()) {
//            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "USERNAME CANNOT BE BLANK", 400);
//        }
//        if (assignRoleRequest.getMasRole().size() <=0) {
//            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "ROLE CANNOT BE BLANK", 400);
//        }
//        String uName="";
//        User emailObj = userRepo.findByUsername(assignRoleRequest.getUsername());
//        if (emailObj == null ) {
//            User mobObj= userRepo.findByPhoneNumber(assignRoleRequest.getUsername());
//            if(mobObj ==null) {
//                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "INVALID USER", 400);
//            }else{
//                uName = mobObj.getUsername();
//            }
//        }else{
//            uName = assignRoleRequest.getUsername();
//        }
//
//
//        User isUser = userRepo.findByUsernameAndStatus(uName, "y");
//        if (isUser == null) {
//            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "ACTIVE USER NOT FOUND FROM THIS USERNAME", 400);
//        }
//
//        try {
//            for (MasRole role : assignRoleRequest.getMasRole()) {
//                UserRole userRole = userRoleRepo.findByUserIdAndRole(isUser, role);
//                if (userRole == null) {
//                    UserRole newUserRole = new UserRole();
//                    newUserRole.setUserId(isUser);
//                    newUserRole.setRole(role);
//                    newUserRole.setStatus("y");
//                    newUserRole.setCreatedAt(Instant.now());
//                    newUserRole.setCreatedBy("0");
//                    userRoleRepo.save(newUserRole);
//                    roleMessages.add("Role: " + role.getRoleDesc() + "successfully  assigned to user: " + isUser.getUsername() +" or " +isUser.getPhoneNumber() );
//                } else {
//                    roleMessages.add("The: " + isUser.getUsername() +" or " +isUser.getPhoneNumber()+ " Have Already this role: " + role.getRoleDesc());
//                }
//            }
//        } catch (SDDException e) {
//            logger.error("An error occurred while assigning roles: {}", e.getMessage());
//            throw e;
//        } catch (Exception e) {
//            logger.error("Unexpected error while assigning roles", e);
//            throw new SDDException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error while assigning roles");
//        }
//        defaultResponse.setMsg(String.join(", ", roleMessages));
//        return ResponseUtils.createSuccessResponse(defaultResponse, new TypeReference<DefaultResponse>() {});
//    }



    @Transactional(rollbackFor = {Exception.class})
    @Override
    public ApiResponse<DefaultResponse> activeInactiveRole(String userName, String roll_id, boolean status) {

        DefaultResponse defaultResponse = new DefaultResponse();

        if (userName == null || userName.isEmpty()) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "USERNAME CANNOT BE BLANK", 400);
        }
        if (roll_id == null || roll_id.isEmpty()) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "ROLE CANNOT BE BLANK", 400);
        }
        String uName="";
        User emailObj = userRepo.findByUserName(userName);
        if (emailObj == null ) {
            User mobObj= userRepo.findByPhoneNumber(userName);
            if(mobObj ==null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "INVALID USER", 400);
            }else{
                uName = mobObj.getUsername();
            }
        }else{
            uName = userName;
        }

        User isUser = userRepo.findByUserNameAndStatus(uName, "y");
        if (isUser == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "ACTIVE USER NOT FOUND FROM THIS USERNAME", 400);
        }
//        MasRole role =masRoleRepo.findByRoleCode(roll_id);
//        if (role == null) {
//            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "INVALID ROLL ID NOT FOUND IN DB", 400);
//        }
//        UserRole userRole =userRoleRepo.findByUserIdAndRole(isUser,role);
//        if (userRole == null) {
//            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "USER NOT HAVE THIS ROLL PLEASE ASSIGN", 400);
//        }

        try {
            String s = status ? "Activated" : "Deactivated";
//            if(status)
//                userRole.setStatus("y");
//            else
//                userRole.setStatus("n");
//            userRole.setLastChangeDate(Instant.now());
//            userRole.setLastChangedBy("0");
//            userRoleRepo.save(userRole);
//            defaultResponse.setMsg("User Role " +s+  " Successfully ,Role:" + role.getRoleDesc());
        } catch (SDDException e) {
            logger.error("An error occurred while Active and Dative User Role: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error while assigning roles", e);
            throw new SDDException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error while assigning roles");
        }
        return ResponseUtils.createSuccessResponse(defaultResponse, new TypeReference<DefaultResponse>() {});
    }



    @Transactional(rollbackFor = {Exception.class})
    @Override
    public ApiResponse<DefaultResponse> activeInactiveUser(String userName, boolean status) {

        DefaultResponse defaultResponse = new DefaultResponse();

        if (userName == null || userName.isEmpty()) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "USERNAME CANNOT BE BLANK", 400);
        }
        String uName="";
        User emailObj = userRepo.findByUserName(userName);
        if (emailObj == null ) {
            User mobObj= userRepo.findByPhoneNumber(userName);
            if(mobObj ==null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "INVALID USER", 400);
            }else{
                uName = mobObj.getUsername();
            }
        }else{
            uName = userName;
        }

        User isUser = userRepo.findByUserName(uName);
        if (isUser == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "USER NOT FOUND FROM THIS USERNAME", 400);
        }

        try {
            String msg = status ? "Activated" : "Deactivated";
            if(status)
                isUser.setStatus("y");
            else
                isUser.setStatus("n");
            isUser.setLastChangeDate(Instant.now());
            isUser.setLastChangedBy("0");
            userRepo.save(isUser);
            defaultResponse.setMsg("User " +msg+  " Successfully ");
//            List<UserRole> userRole =userRoleRepo.findByUserId(isUser);
//            for(UserRole obj:userRole){
//                if (!status){
//                    obj.setStatus("n");
//                    obj.setLastChangeDate(Instant.now());
//                    obj.setLastChangedBy("0");
//                    userRoleRepo.save(obj);
//                    defaultResponse.setMsg("User All Role Deactivated Successfully");
//                }
//            }
        } catch (SDDException e) {
            logger.error("An error occurred while Active Dative User: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error while assigning roles", e);
            throw new SDDException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error while assigning roles");
        }
        return ResponseUtils.createSuccessResponse(defaultResponse, new TypeReference<DefaultResponse>() {});
    }



    @Override
    public ApiResponse<List<User>> getAllUser() {
        List<User> User;
        try {
            User = userRepo.findAll();

            if (User.isEmpty()) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "No User found", 400);
            }

        } catch (SDDException e) {
            logger.error("An error occurred while fetching User: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error while fetching User", e);
            throw new SDDException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error while fetching User");
        }

        return ResponseUtils.createSuccessResponse(User, new TypeReference<List<User>>() {});
    }



    @Transactional(rollbackFor = {Exception.class})
    public void saveLoginAudit(String loginType, String username, String userLogin, String role, String token, long expirationTime) {
        DefaultResponse defaultResponse = new DefaultResponse();

        if (username == null || username.isEmpty()) {
            throw new SDDException(HttpStatus.BAD_REQUEST.value(), "USER NAME CAN NOT BE BLANK");
        }
        if (role == null || role.isEmpty()) {
            throw new SDDException(HttpStatus.BAD_REQUEST.value(), " USER ROLE CAN NOT BE BLANK");
        }
        if (token == null || token.isEmpty()) {
            throw new SDDException(HttpStatus.BAD_REQUEST.value(), " TOKEN CAN NOT BE BLANK");
        }
        if (expirationTime == 0 ) {
            throw new SDDException(HttpStatus.BAD_REQUEST.value(), " TOKEN TIMESTAMP CAN NOT BE BLANK");
        }

        User isUser = userRepo.findByUserName(username);
        if (isUser == null) {
            throw new SDDException(HttpStatus.UNAUTHORIZED.value(), " USER NOT FOUND FROM THIS EMAIL ID");
        }

//        MasRole roleObj =masRoleRepo.findByRoleCode(role);
//        if (roleObj == null) {
//            throw new SDDException(HttpStatus.UNAUTHORIZED.value(), " ROLE CODE IS INCORRECT");
//        }

        String ipAddress = httpServletRequest.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = httpServletRequest.getHeader("X-Real-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = httpServletRequest.getRemoteAddr();
        }

        try {
//            LoginAudit obj = new LoginAudit();
//            obj.setUser(isUser);
//            obj.setUserName(userLogin);
//            obj.setRole(roleObj);
//            obj.setIpAddress(ipAddress);
//            obj.setToken(token);
//            obj.setTokenExpirationTime(Instant.ofEpochMilli(expirationTime));
//            obj.setLoginTimestamp(Instant.now());
//            obj.setLoginSuccess(true);
//            obj.setLogoutTimestamp(new Timestamp(expirationTime));
//            obj.setTokenBlacklisted(false);
//            obj.setLoginMethod(loginType);
//            obj.setAuthMethod(" ");
//            obj.setSocialMediaUserId(" ");
//            obj.setDeviceDetails(" ");
//            obj.setFailureReason(" ");
//            obj.setCreatedAt(Instant.now());
//            loginAuditRepo.save(obj);
            defaultResponse.setMsg("USER Login Details Capture successfully");
        } catch (Exception e) {
            logger.error("An error occurred while saving User LoginAudit", e);
        }
    }

    public ApiResponse<Long> getActiveUserCount() {
//        System.out.println("hello time---"+HelperUtils.getCurrentTimeStamp());
//        long cont= loginAuditRepo.countActiveUser(HelperUtils.getCurrentTimeStamp());
//        System.out.println("hello count----"+cont);
//        logger.error("An error occurred while count the current User");
//        return ResponseUtils.createSuccessResponse(cont, new TypeReference<Long>() {
//        });
        return null;
    }



    @Override
    public ApiResponse<String> getCurrentUser(Principal principal) {
        if (principal.getName() == null || principal.getName().isEmpty()) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "USERNAME CAN NOT BE BLANK", 400);
        }
        User emailObj= userRepo.findByUserName(principal.getName());
        if (emailObj== null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "CURRENT USER NOT FOUND", 400);
        }
        String name= principal.getName() +" or "+emailObj.getPhoneNumber();
        return ResponseUtils.createSuccessResponse(name, new TypeReference<String>() {
        });
    }


    @Override
    public ApiResponse<DefaultResponse> logout(HttpServletRequest request) {
        DefaultResponse resp=new DefaultResponse();
        String token = extractTokenFromRequest(request);
        if (token == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "Invalid token", 400);
        }
        long expirationTime = helper.getExpirationTime(token);
        tokenBlacklistService.addToBlacklist(token, expirationTime); // Add to in-memory blacklist
//        LoginAudit activeUser = loginAuditRepo.findByToken(token);
//        if (activeUser != null) {
//            activeUser.setLogoutTimestamp(Instant.now());
//            activeUser.setTokenBlacklisted(true);
//            loginAuditRepo.save(activeUser);
//            resp.setMsg("Logged out successfully");
//        }// Mark as blacklisted in the database
        return ResponseUtils.createSuccessResponse(resp, new TypeReference<DefaultResponse>() {
        });
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }



    @Transactional(rollbackFor = {Exception.class})
    @Override
    public ApiResponse<DefaultResponse> changePassword(PasswordChangeReq request) {

        DefaultResponse resp=new DefaultResponse();
        if (request.getUsername() == null || request.getUsername().isEmpty()) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "USERNAME CAN NOT BE BLANK", 400);
        }
        if (request.getCurrentPassword() == null || request.getCurrentPassword().isEmpty()) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "CURRENT PASSWORD CAN NOT BE BLANK", 400);
        }
        if (request.getNewPassword() == null || request.getNewPassword().isEmpty()) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "NEW PASSWORD CAN NOT BE BLANK", 400);
        }
        String uName="";
        User emailObj = userRepo.findByUserName(request.getUsername());
        if (emailObj == null ) {
            User mobObj= userRepo.findByPhoneNumber(request.getUsername());
            if(mobObj ==null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "INVALID USER", 400);
            }else{
                uName = mobObj.getUsername();
            }
        }else{
            uName = request.getUsername();
        }

        User isUser = userRepo.findByUserNameAndStatus(uName,"y");
        if (isUser == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "ACTIVE USER NOT FOUND FROM THIS USERNAME", 400);
        }

        try {
            boolean isPasswordMatch = passwordEncoder.matches(request.getCurrentPassword(), isUser.getCurrentPassword());
            boolean newpswd = passwordEncoder.matches(request.getNewPassword(), isUser.getCurrentPassword());

            if (isPasswordMatch) {
                if(newpswd){
                    resp.setMsg("CURRENT PASSWORD & NEW PASSWORD CAN NOT BE SAME");
                }else{
                    isUser.setCurrentPassword(passwordEncoder.encode(request.getNewPassword()));
                    isUser.setOldPassword(passwordEncoder.encode(request.getNewPassword()));
                    isUser.setLastChangeDate(Instant.now());
                    userRepo.save(isUser);
                    resp.setMsg("PASSWORD CHANGED SUCCESSFULLY");
                }
            } else {
                resp.setMsg("CURRENT PASSWORD NOT MATCHED");
            }
        } catch (Exception e) {
            logger.error("An error occurred while changing password", e);
        }
        return ResponseUtils.createSuccessResponse(resp, new TypeReference<DefaultResponse>() {
        });
    }



    @Transactional(rollbackFor = {Exception.class})
    @Override
    public ApiResponse<DefaultResponse> resetPassword(ResetPasswordReq resetPasswordReq) {
        DefaultResponse objMsg =new DefaultResponse();

        if(resetPasswordReq.getUsername()==null ||resetPasswordReq.getUsername().isEmpty()){
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "USERNAME CAN NOT BE BLANK", 400);
        }
        if(resetPasswordReq.getOtp()==null ||resetPasswordReq.getOtp().isEmpty()){
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "OTP CAN NOT BE BLANK", 400);
        }
        if(resetPasswordReq.getNewPassword()==null ||resetPasswordReq.getNewPassword().isEmpty()){
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "NEW PASSWORD NOT NOT BE BLANK", 400);
        }
        if(resetPasswordReq.getReEnterPassword()==null ||resetPasswordReq.getReEnterPassword().isEmpty()){
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "RE-ENTER PASSWORD NOT NOT BE BLANK", 400);
        }
        if(!resetPasswordReq.getNewPassword().equals(resetPasswordReq.getReEnterPassword())){
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "NEW PASSWORD & RE-ENTER PASSWORD MUST BE SAME", 400);
        }
        String uName="";
        User emailObj = userRepo.findByUserName(resetPasswordReq.getUsername());
        if (emailObj == null ) {
            User mobObj= userRepo.findByPhoneNumber(resetPasswordReq.getUsername());
            if(mobObj ==null) {
                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "INVALID USER", 400);
            }else{
                uName = mobObj.getUsername();
            }
        }else{
            uName = resetPasswordReq.getUsername();
        }
        User isUser = userRepo.findByUserNameAndStatus(uName,"y");
        if (isUser == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "ACTIVE USER NOT FOUND FROM THIS USERNAME", 400);
        }

        String userOtp=isUser.getTempOtp();
        if(userOtp==null ||userOtp.isEmpty()){
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "OTP NOT FOUND FROM DB", 400);
        }
        Timestamp time= Timestamp.from(isUser.getLastChangeDate());

        try {
            Timestamp currentTime= HelperUtils.getCurrentTimeStamp();
            if(userOtp.equals(resetPasswordReq.getOtp())){
                if(currentTime.getTime() -time.getTime() > 600000){
                    objMsg.setMsg("Your OTP has Expired");
                }else {
                    isUser.setCurrentPassword(passwordEncoder.encode(resetPasswordReq.getNewPassword()));
                    isUser.setOldPassword(passwordEncoder.encode(resetPasswordReq.getNewPassword()));
                    userRepo.save(isUser);
                    objMsg.setMsg("Your Password has been Successfully Reset");
                }
            }else{
                objMsg.setMsg("INVALID OTP");
            }
        } catch (Exception e) {
            logger.error("An error occurred while reset password", e);
        }
        return ResponseUtils.createSuccessResponse(objMsg, new TypeReference<DefaultResponse>() {
        });
    }

//    @Transactional(rollbackFor = {Exception.class})
//    @Override
//    public ApiResponse<DefaultResponse> sendOTP(String username) {
//        DefaultResponse objMsg =new DefaultResponse();
//
//        if(username==null ||username.isEmpty()){
//            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "USERNAME CAN NOT BE BLANK", 400);
//        }
//        String uName="";
//        User emailObj = userRepo.findByUsername(username);
//        if (emailObj == null ) {
//            User mobObj= userRepo.findByPhoneNumber(username);
//            if(mobObj ==null) {
//                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "INVALID USER", 400);
//            }else{
//                uName = mobObj.getUsername();
//            }
//        }else{
//            uName = username;
//        }
//
//        User isUser = userRepo.findByUsernameAndStatus(uName,true);
//        if (isUser == null) {
//            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "ACTIVE USER NOT FOUND FROM THIS EMAIL ID", 400);
//        }
//
//        try {
//            String otp=HelperUtils.getOtp();
//            isUser.setLastChangeDate(HelperUtils.getCurrentTimeStamp());
//            isUser.setOtp(otp);
//            userRepo.save(isUser);
//            String msg= otp +" "+"is The One Time Password(OTP) for Reset Your Password.This OTP is usable only once and is valid for 10 min. PLS DO NOT SHARE THE OTP WITH ANYONE -Intek Micro Pvt Ltd ";
//            sendEmail(username,"Reset Password",msg);
//            objMsg.setMsg("OTP sent Successfully on your Registered "+isUser.getUsername());
//        } catch (Exception e) {
//            logger.error("An error occurred while sending OTP on email", e);
//        }
//        return ResponseUtils.createSuccessResponse(objMsg, new TypeReference<DefaultResponse>() {
//        });
//    }




    // Send OTP using a third-party API
//    @Transactional(rollbackFor = {Exception.class})
//    @Override
//    public ApiResponse<DefaultResponse> sendOtp(String username) {
//        DefaultResponse objMsg =new DefaultResponse();
//        if(username==null ||username.isEmpty()){
//            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "USERNAME CAN NOT BE BLANK", 400);
//        }
//        String uName="";
//        User emailObj = userRepo.findByUsername(username);
//        if (emailObj == null ) {
//            User mobObj= userRepo.findByPhoneNumber(username);
//            if(mobObj ==null) {
//                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "INVALID USER", 400);
//            }else{
//                uName = mobObj.getUsername();
//            }
//        }else{
//            uName = username;
//        }
//        User isUser = userRepo.findByUsernameAndStatus(uName,"y");
//        if (isUser == null) {
//            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {}, "ACTIVE USER NOT FOUND FROM THIS USERNAME", 400);
//        }
//
//        Map<String, String> request = new HashMap<>();
//        request.put("mobileNo", isUser.getPhoneNumber());
//        request.put("emailId", isUser.getUsername());
//        request.put("apiKey", apiKey);
//
//        try {
//            ResponseEntity<Map> response = restTemplate.postForEntity(sendOtpUrl, request, Map.class);
//            if(response.getStatusCode() == HttpStatus.OK && (boolean) Objects.requireNonNull(response.getBody()).get("success")){
//                objMsg.setMsg("OTP sent Successfully on your Registered "+isUser.getUsername() +" or "+isUser.getPhoneNumber());
//            }else{
//                objMsg.setMsg("Unable to sent OTP on your Registered "+isUser.getUsername() +" or "+isUser.getPhoneNumber());
//            }
//        } catch (Exception e) {
//            logger.error("An error occurred while sending OTP on email or mobile", e);
//        }
//        return ResponseUtils.createSuccessResponse(objMsg, new TypeReference<DefaultResponse>() {
//        });
//    }



    // Validate OTP using a third-party API
//    @Transactional(rollbackFor = {Exception.class})
//    public boolean validateOtp(String username, String otp) {
//        if(username==null ||username.isEmpty()){
//            throw new SDDException(HttpStatus.UNAUTHORIZED.value(), "USERNAME CAN NOT BE BLANK");
//        }
//        if(otp==null ||otp.isEmpty()){
//            throw new SDDException(HttpStatus.UNAUTHORIZED.value(), "OTP CAN NOT BE BLANK");
//        }
//        String uName="";
//        User emailObj = userRepo.findByUsername(username);
//        if (emailObj == null ) {
//            User mobObj= userRepo.findByPhoneNumber(username);
//            if(mobObj ==null) {
//                throw new SDDException(HttpStatus.UNAUTHORIZED.value(), "INVALID USER");
//            }else{
//                uName = mobObj.getUsername();
//            }
//        }else{
//            uName = username;
//        }
//        User isUser = userRepo.findByUsernameAndStatus(uName,"y");
//        if (isUser == null) {
//            throw new SDDException(HttpStatus.UNAUTHORIZED.value(), "ACTIVE USER NOT FOUND FROM THIS USERNAME");
//        }
//        Map<String, String> request = new HashMap<>();
//        request.put("mobileNo", isUser.getPhoneNumber());
//        request.put("emailId", isUser.getUsername());
//        request.put("otp", otp);
//        request.put("apiKey", apiKey);
//
//        try {
//            ResponseEntity<Map> response = restTemplate.postForEntity(validateOtpUrl, request, Map.class);
//            return response.getStatusCode() == HttpStatus.OK && (boolean) Objects.requireNonNull(response.getBody()).get("valid");
//        } catch (Exception e) {
//            logger.error("An error occurred while validate OTP from email or mobile", e);
//            return false;
//        }
//    }





}
