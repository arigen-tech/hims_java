package com.hims.jwt;



import com.hims.entity.User;
import com.hims.entity.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomeUserDetailsImpl implements UserDetailsService {

    @Autowired
    private UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepo.findByUserNameAndStatus(username,"y");
        if (user== null) {
            throw new UsernameNotFoundException("User Not found !!");
        }
        System.out.println("Hello world");
        return user;
    }

}
