package com.hims.entity.repository;


import com.hims.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {

    User findByUserNameAndStatus(String username , String isActive);

    User findByUserName(String email);

    User findByPhoneNumber(String phoneNo);

    User findByMobileNo(String mobileNo);


    User findByPhoneNumberAndStatus(String userName , String isActive);
}
