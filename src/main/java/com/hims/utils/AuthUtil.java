package com.hims.utils;

import com.hims.entity.User;
import com.hims.entity.repository.UserRepo;
import com.hims.service.impl.AppSetupServicesImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class AuthUtil {
    private static final Logger log = LoggerFactory.getLogger(AuthUtil.class);


    @Autowired
    private UserRepo userRepo;

    @Value("${jwt.secret}")
    private String secret;

    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (username == null) return null;
        return userRepo.findByUserName(username);
    }

    public Long getCurrentDepartmentId() {
        return getDepartmentIdFromToken();
    }

    private String getTokenFromRequest() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private Long getDepartmentIdFromToken() {
        try {
            String token = getTokenFromRequest();
            if (token == null) return null;

            Claims claims = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();

            Object deptIdObj = claims.get("departmentId");
            return deptIdObj != null ? Long.parseLong(deptIdObj.toString()) : null;
        } catch (Exception e) {
            log.error("Error extracting departmentId", e);
            return null;
        }
    }
}
