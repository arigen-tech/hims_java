package com.hims.service;

import com.hims.entity.User;
import com.hims.entity.repository.UserRepo;
import com.hims.projection.UserContextProjection;
import com.hims.response.UserContext;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Service
public class UserContextService {

    @Value("${jwt.secret}")
    private String secret;

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

    private Long getDepartmentIdFromToken() {
        Claims claims = getCurrentTokenClaims();
        if (claims == null) {
            return null;
        }

        try {
            Object deptIdObj = claims.get("departmentId");
            return deptIdObj != null ? Long.parseLong(deptIdObj.toString()) : null;
        } catch (NumberFormatException ex) {
            log.debug("Invalid departmentId claim in JWT: {}", ex.getMessage());
            return null;
        }
    }

    public UserContext getCurrentUserContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            return null;
        }
        String username = authentication.getName();
        UserContext context = getUserContextByUsername(username);

        if (context != null) {
            context.setDepartmentId(getCurrentDepartmentId());
        }
        return context;
    }


    private String getTokenFromRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }

        HttpServletRequest request = attributes.getRequest();
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public Long getCurrentDepartmentId() {
        return getDepartmentIdFromToken();
    }

    public String getCurrentUserFullNameFromToken() {
        Claims claims = getCurrentTokenClaims();
        return claims == null ? null : claims.get("name", String.class);
    }

    private Claims getCurrentTokenClaims() {
        String token = getTokenFromRequest();
        if (!StringUtils.hasText(token)) {
            return null;
        }

        try {
            return Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Unable to parse current JWT: {}", ex.getMessage());
            return null;
        }
    }

}


