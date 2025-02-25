package com.hims.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtHelper jwtHelper;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                handlePreflightRequest(response);
                return;
            }

            String authorizationHeader = request.getHeader("Authorization");
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7);

                // Check if token is blacklisted
                if (tokenBlacklistService.isBlacklisted(token)) {
                    logger.info("Token is blacklisted.");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }

                // Extract username and validate token
                String username = extractUsernameFromToken(token);
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    authenticateUser(request, token, username);
                }
            } else {
                logger.info("Invalid or missing Authorization header.");
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            logger.error("Error occurred while processing the JWT token.", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handlePreflightRequest(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private String extractUsernameFromToken(String token) {
        try {
            return jwtHelper.getUsernameFromToken(token);
        } catch (IllegalArgumentException e) {
            logger.error("Illegal argument while fetching username from token.", e);
        } catch (ExpiredJwtException e) {
            logger.warn("JWT token is expired.", e);
        } catch (MalformedJwtException e) {
            logger.error("Malformed JWT token.", e);
        } catch (Exception e) {
            logger.error("Unexpected error while fetching username from token.", e);
        }
        return null;
    }

    private void authenticateUser(HttpServletRequest request, String token, String username) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (jwtHelper.validateToken(token, userDetails)) {
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.info("User authenticated successfully: {}", username);
        } else {
            logger.warn("Token validation failed for user: {}", username);
        }
    }
}
