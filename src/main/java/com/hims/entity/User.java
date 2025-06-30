package com.hims.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.Instant;
import java.util.Collection;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "status", nullable = false, length = 1)
    private String status;

    @Column(name = "user_name", nullable = false, length = 100)
    private String userName;

    @Column(name = "mobile_no", length = 15)
    private String mobileNo;

    @Column(name = "last_chg_by", length = 200)
    private String lastChangedBy;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "created_by", length = 255)
    private String createdBy;

    @Column(name = "current_password", length = 255)
    private String currentPassword;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "first_name", length = 255)
    private String firstName;

    @Column(name = "is_verified")
    private Boolean isVerified;

    @Column(name = "last_chg_date")
    private Instant lastChangeDate;

    @Column(name = "last_name", length = 255)
    private String lastName;

    @Column(name = "middle_name", length = 255)
    private String middleName;

    @Column(name = "profile_picture", length = 255)
    private String profilePicture;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "verification_method", length = 20)
    private String verificationMethod;

    @Column(name = "user_flag", nullable = false, columnDefinition = "integer default 0")
    private Integer userFlag;

    @Column(name = "level_of_user", length = 1)
    private String levelOfUser;

    @Column(name = "role_id", length = 255)
    private String roleId;

    @Column(name = "aadhar_number", length = 12)
    private String aadharNumber;

    @Column(name = "address_info", length = 255)
    private String addressInfo;

    @Column(name = "nationality", length = 100)
    private String nationality;

    @Column(name = "old_password", length = 255)
    private String oldPassword;

    @Column(name = "temp_otp", length = 255)
    private String tempOtp;

    @Column(name = "pan_number", length = 10)
    private String panNumber;

    @Column(name = "passport_expiry_date")
    private Instant passportExpiryDate;

    @Column(name = "passport_number", length = 20)
    private String passportNumber;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "pin_code", length = 6)
    private String pinCode;

    @Column(name = "social_media_provider", length = 50)
    private String socialMediaProvider;

    @Column(name = "social_media_user_id", length = 255)
    private String socialMediaUserId;

    @ManyToOne
    @JoinColumn(name = "employee_id", referencedColumnName = "emp_id", nullable = false)
    private MasEmployee employee;

    @ManyToOne
    @JoinColumn(name = "hospital_id", referencedColumnName = "hospital_id", nullable = false)
    private MasHospital hospital;

    @ManyToOne
    @JoinColumn(name = "user_type_id", referencedColumnName = "user_type_id", nullable = false)
    private MasUserType userType;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return this.currentPassword;
    }

    @Override
    public String getUsername() {
        return this.userName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
