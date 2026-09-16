package com.hims.service;

import com.hims.entity.User;
import com.hims.entity.repository.UserRepo;
import com.hims.projection.UserContextProjection;
import com.hims.response.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserContextService {

    @Autowired
    private UserRepo userRepository;

    @Cacheable(value = "userDetails", key = "#username")
    public User getUserByUsername(String username) {
        return userRepository.findByUserName(username);
    }

    @Cacheable(value = "userContext", key = "#username")
    public UserContext getUserContextByUsername(String username) {
        UserContextProjection projection = userRepository.findUserContextByUserName(username);

        if (projection == null) {
            return null;
        }

        return new UserContext(
                projection.getUserId(),
                projection.getUserName(),
                projection.getEmail(),
                projection.getHospitalId(),
                projection.getDepartmentId(),
                projection.getUserFullName()
        );
    }

    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (username == null) return null;
        return getUserByUsername(username);
    }

    public UserContext getCurrentUserContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            return null;
        }
        String username = authentication.getName();
        return getUserContextByUsername(username);
    }
}
