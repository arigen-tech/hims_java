package com.hims.jwt;

import com.hims.entity.Patient;
import com.hims.entity.repository.PatientRepository;
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
    private PatientRepository patientRepository;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//        try {
////            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
////                handlePreflightRequest(response);
////                return;
////            }
//
//            String authorizationHeader = request.getHeader("Authorization");
//            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
//                String token = authorizationHeader.substring(7);
//
//                // Check if token is blacklisted
//                if (tokenBlacklistService.isBlacklisted(token)) {
//                    logger.info("Token is blacklisted.");
//                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                    return;
//                }
//
//                // Extract username and validate token
//                if (SecurityContextHolder.getContext().getAuthentication() == null) {
//                    if ("PATIENT".equals(jwtHelper.getClaimFromToken(token,
//                            claims -> claims.get("principalType", String.class)))) {
//                        authenticatePatient(request, token);
//                    } else {
//                        String username = extractUsernameFromToken(token);
//                        if (username != null) {
//                            authenticateUser(request, token, username);
//                        }
//                    }
//                }
//            } else {
//                logger.info("Invalid or missing Authorization header.");
//            }
//
//            filterChain.doFilter(request, response);
//        } catch (Exception e) {
//            logger.error("Error occurred while processing the JWT token.", e);
//            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//        }
//    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String authorizationHeader = request.getHeader("Authorization");

            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7).trim();

                // ---- Guard against placeholder / malformed tokens ----
                if (!isValidJwtFormat(token)) {
                    logger.debug("Skipping JWT processing: token is not a valid JWT (starts with '{}')",
                            token.isEmpty() ? "<empty>" : token.substring(0, Math.min(10, token.length())));
                    filterChain.doFilter(request, response);
                    return;
                }

                // Check if token is blacklisted
                if (tokenBlacklistService.isBlacklisted(token)) {
                    logger.info("Token is blacklisted.");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }

                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    // Wrap claim lookups so a bad token never 500s
                    String principalType = safeGetClaim(token,
                            claims -> claims.get("principalType", String.class));

                    if ("PATIENT".equals(principalType)) {
                        authenticatePatient(request, token);
                    } else {
                        String username = extractUsernameFromToken(token);
                        if (username != null) {
                            authenticateUser(request, token, username);
                        }
                    }
                }
            } else {
                logger.debug("No Bearer token present; continuing unauthenticated.");
            }

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            logger.warn("JWT expired: {}", e.getMessage());
            filterChain.doFilter(request, response);   // stay unauthenticated
        } catch (Exception e) {
            // Don't 500 on bad tokens — just continue and let Spring Security decide
            logger.warn("Ignoring invalid JWT: {}", e.getMessage());
            filterChain.doFilter(request, response);
        }
    }

    private boolean isValidJwtFormat(String token) {
        if (token == null || token.isBlank()) return false;
        if ("null".equalsIgnoreCase(token) || "undefined".equalsIgnoreCase(token)) return false;

        // A JWT must have exactly 2 dots
        int firstDot = token.indexOf('.');
        if (firstDot <= 0) return false;
        int secondDot = token.indexOf('.', firstDot + 1);
        if (secondDot <= firstDot + 1) return false;
        if (token.indexOf('.', secondDot + 1) != -1) return false;
        if (secondDot == token.length() - 1) return false;

        return true;
    }

    /**
     * Reads a claim without throwing if the token is invalid/expired.
     */
    private <T> T safeGetClaim(String token, java.util.function.Function<io.jsonwebtoken.Claims, T> resolver) {
        try {
            return jwtHelper.getClaimFromToken(token, resolver);
        } catch (ExpiredJwtException e) {
            logger.warn("JWT expired while reading claim: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.warn("Malformed JWT: {}", e.getMessage());
        } catch (Exception e) {
            logger.warn("Failed to read JWT claim: {}", e.getMessage());
        }
        return null;
    }


//    private void handlePreflightRequest(HttpServletResponse response) {
//        response.setHeader("Access-Control-Allow-Origin", "*");
//        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
//        response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
//        response.setStatus(HttpServletResponse.SC_OK);
//    }

    private void handlePreflightRequest(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods",
                "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers",
                "*");
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

    private void authenticatePatient(HttpServletRequest request, String token) {
        Long patientId = jwtHelper.getClaimFromToken(token,
                claims -> claims.get("patientId", Long.class));
        if (patientId == null) {
            logger.warn("Patient token does not contain patientId");
            return;
        }

        Patient patient = patientRepository.findById(patientId).orElse(null);
        if (patient == null) {
            logger.warn("Patient not found for patientId: {}", patientId);
            return;
        }

        PatientPrincipal patientPrincipal = new PatientPrincipal(patient);
        if (jwtHelper.validateToken(token, patientPrincipal)) {
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(patientPrincipal, null,
                            patientPrincipal.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.info("Patient authenticated successfully: {}", patientId);
        }
    }
}
