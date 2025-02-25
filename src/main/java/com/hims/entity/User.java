package com.hims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;


@Getter
@Setter
@Entity
@Data
@NoArgsConstructor
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Size(max = 12)
    @Column(name = "aadhar_number", length = 12)
    private String aadharNumber;

    @Size(max = 255)
    @Column(name = "address_info")
    private String addressInfo;

    @Column(name = "created_at")
    private Instant createdAt;

    @Size(max = 255)
    @Column(name = "created_by")
    private String createdBy;

    @Size(max = 255)
    @Column(name = "current_password")
    private String curPassword;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Size(max = 255)
    @Column(name = "first_name")
    private String firstName;

    @Size(max = 255)
    @Column(name = "email")
    private String username;

    @Size(max = 20)
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Size(max = 255)
    @Column(name = "old_password")
    private String oldPassword;

    @Column(name = "is_verified")
    private Boolean isVerified;

    @Size(max = 255)
    @Column(name = "middle_name")
    private String middleName;

    @Size(max = 255)
    @Column(name = "last_chg_by")
    private String lastChgBy;

    @Column(name = "last_chg_date")
    private Instant lastChgDate;

    @Size(max = 255)
    @Column(name = "last_name")
    private String lastName;
//
//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "gender_id")
//    private MasGender gender;

    @Size(max = 100)
    @Column(name = "nationality", length = 100)
    private String nationality;

    @Size(max = 6)
    @Column(name = "pin_code", length = 6)
    private String pinCode;

//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "state_id")
//    private MasState state;

    @Size(max = 255)
    @Column(name = "profile_picture")
    private String profilePicture;

    @Size(max = 20)
    @Column(name = "verification_method", length = 20)
    private String verificationMethod;

    @Size(max = 10)
    @Column(name = "pan_number", length = 10)
    private String panNumber;

    @Column(name = "passport_expiry_date")
    private LocalDate passportExpiryDate;

    @Size(max = 20)
    @Column(name = "passport_number", length = 20)
    private String passportNumber;

//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "issue_authority")
//    private MasCountry issueAuthority;

    @Size(max = 50)
    @Column(name = "social_media_provider", length = 50)
    private String socialMediaProvider;

    @Size(max = 255)
    @Column(name = "social_media_user_id")
    private String socialMediaUserId;


    @Size(max = 255)
    @Column(name = "temp_otp")
    private String otp;

    @Size(max = 1)
    @Column(name = "status", length = 1)
    private String status;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return this.curPassword;
    }

    @Override
    public String getUsername() {
        return this.username;
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
