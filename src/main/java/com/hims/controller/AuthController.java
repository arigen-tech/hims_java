package com.hims.controller;


import com.hims.entity.User;
import com.hims.entity.repository.UserRepo;
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
import com.hims.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.List;

//@CrossOrigin(origins = "${angular.corss.url}")
@RestController
@Tag(name = "authController", description = "This controller only for use User Related any task.")
@RequestMapping("/authController")
@Slf4j
public class AuthController {
    @Autowired
    private UserRepo userRepo;

    @Autowired
    private AuthService authService;

    private final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Operation(summary = "This API is used to create first users without need any token")
    @PostMapping("/create-first-users")
    public ResponseEntity<ApiResponse<DefaultResponse>> createUser(@RequestBody UserCreationReq userCreationReq) {
        return new ResponseEntity<>(authService.createFirstUser(userCreationReq), HttpStatus.OK);
    }

    @Operation(summary = "This API is used to Login with password for get Token for access any API.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponce>> login(@RequestBody JwtRequest request) {
        return new ResponseEntity<>(authService.login(request), HttpStatus.OK);
    }

//    @Operation(summary = "This API is used to Login with OTP for get Token for access any API.")
//    @PostMapping("/loginWithOtp")
//    public ResponseEntity<ApiResponse<JwtResponce>>loginWithOtp(@RequestBody OtpRequest request) {
//        return new ResponseEntity<>(authService.loginWithOtp(request), HttpStatus.OK);
//    }

    @Operation(summary = "This API is used to get user using username")
    @GetMapping("/getUsers/{userName}")
    public ResponseEntity<ApiResponse<User>> getUsers(@PathVariable(value = "userName") String userName) {
        return new ResponseEntity<>(authService.getUser(userName), HttpStatus.OK);
    }

    @GetMapping("/getUsersForProfile/{username}")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUserForProfile(@PathVariable(value = "username") String userName) {
        return new ResponseEntity<>(authService.getUserForProfile(userName), HttpStatus.OK);
    }

    @Operation(summary = "This API is used to create users")
    @PostMapping("/createUsers")
    public ResponseEntity<ApiResponse<DefaultResponse>> createUsers(@RequestBody UserDetailsReq userDetailsReq) {
        return new ResponseEntity<>(authService.createUser(userDetailsReq), HttpStatus.OK);
    }

    @Operation(summary = "This API is used to get update users")
    @PutMapping("/updateUsers")
    public ResponseEntity<ApiResponse<DefaultResponse>> updateUsers(@RequestBody UserDetailsReq userDetailsReq) {
        return new ResponseEntity<>(authService.updateUser(userDetailsReq), HttpStatus.OK);
    }

//    @Operation(summary = "This API is used to Assign users role")
//    @PostMapping("/assignUserRole")
//    public ResponseEntity<ApiResponse<DefaultResponse>> assignUserRole(@RequestBody AssignRoleRequest assignRoleRequest) {
//        return new ResponseEntity<>(authService.assignUserRole(assignRoleRequest), HttpStatus.OK);
//    }

    @Operation(summary = "This API is used to get users role ")
    @GetMapping("/getUsersRole/{userName}")
    public ResponseEntity<ApiResponse<List<RoleInfoResp>>> getUsersRole(@PathVariable(value = "userName") String userName) {
        return new ResponseEntity<>(authService.getRole(userName), HttpStatus.OK);
    }

    @Operation(summary = "This API is used to get all users list")
    @GetMapping("/getAllUsers")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        return new ResponseEntity<>(authService.getAllUser(), HttpStatus.OK);
    }

    @Operation(summary = "This API is used to get only current active users")
    @GetMapping("/activeUsers")
    public ResponseEntity<ApiResponse<Long>> getActiveUserCount() {
        return ResponseEntity.ok(authService.getActiveUserCount());
    }

    @Operation(summary = "This API is used to change users login password")
    @PostMapping("/changePassword")
    public ResponseEntity<ApiResponse<DefaultResponse>> changePassword(@RequestBody PasswordChangeReq request) {
        return new ResponseEntity<>(authService.changePassword(request), HttpStatus.OK);
    }

    @Operation(summary = "This API is used to reset users login password")
    @PostMapping("/resetPassword")
    public ResponseEntity<ApiResponse<DefaultResponse>> resetPassword(@RequestBody ResetPasswordReq resetPasswordReq) {
        return new ResponseEntity<>(authService.resetPassword(resetPasswordReq), HttpStatus.OK);
    }

//    @Operation(summary = "This API is used to send OTP for reset users login password")
//    @GetMapping("/sendOtp/{username}")
//    public ResponseEntity<ApiResponse<DefaultResponse>> sendOtp(@PathVariable(value = "username") String username) {
//        return new ResponseEntity<>(authService.sendOtp(username), HttpStatus.OK);
//    }

    @Operation(summary = "This API is used to get Current User")
    @GetMapping("/currentUser")
    public ResponseEntity<ApiResponse<String>> getCurrentUsers(Principal principal) {
        return new ResponseEntity<>(authService.getCurrentUser(principal), HttpStatus.OK);
    }

    @Operation(summary = "This API is used to change users status active or inactive")
    @PutMapping("/activeInactiveUser/{userName}/{status}")
    public ResponseEntity<ApiResponse<DefaultResponse>> activeInactiveUser(@PathVariable(value = "userName") String userName,@PathVariable(value = "status") boolean status) {
        return new ResponseEntity<>(authService.activeInactiveUser(userName,status), HttpStatus.OK);
    }

    @Operation(summary = "This API is used to change users role status active or inactive")
    @PutMapping("/activeInactiveRole/{userName}/{roll_Id}/{status}")
    public ResponseEntity<ApiResponse<DefaultResponse>> activeInactiveRole(@PathVariable(value = "userName") String userName,@PathVariable(value = "roll_Id") String roll_Id,@PathVariable(value = "status") boolean status) {
        return new ResponseEntity<>(authService.activeInactiveRole(userName,roll_Id,status), HttpStatus.OK);
    }

    @Operation(summary = "This API is used to logout users")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<DefaultResponse>> logout(HttpServletRequest request) {
        return new ResponseEntity<>(authService.logout(request), HttpStatus.OK);
    }


    @GetMapping("/getProfileImageSrc/{username}")
    public ResponseEntity<?> getProfileImageSrc(@PathVariable String username) {
        User user = userRepo.findByUserName(username);
        String imagePath = user.getProfilePicture();
        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Image not found");
        }
        try {
            Path path = Paths.get(imagePath);
            byte[] imageBytes = Files.readAllBytes(path);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(imageBytes);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error reading image");
        }
    }


}
