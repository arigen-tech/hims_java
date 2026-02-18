package com.hims.jwt;



import com.hims.entity.User;
import com.hims.entity.repository.MasEmployeeRepository;
import com.hims.entity.repository.MasHospitalRepository;
import com.hims.entity.repository.MasUserTypeRepository;
import com.hims.entity.repository.UserRepo;
import com.hims.exception.SDDException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtHelper {


    public static final long JWT_TOKEN_VALIDITY = 7 * 24 * 60 * 60;
    public static final long REFRESH_TOKEN_VALIDITY = 7 * 24 * 60 * 60;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private MasHospitalRepository masHospitalRepository;

    @Autowired
    private MasEmployeeRepository masEmployeeRepository;

//     private final String secret = "afafasfafafasfasfasfafacasdasfasxASFACASDFACASDFASFASFDAFASFASDAADSCSDFADCVSGCFVADXCcadwavfsfarvf";
private static final  String secret = "1KCrT4BFo9EMUNJjQ0y8VswrKFSJmIHp1jZJVP1IU5999EOqb3E1gmNpf5FzYXIZrwpPDHLhRcORigN84ftPfuOt2Q2IKTmRfJP5RRhRCfJJ2wJ4vlMK70fWFeIT5QBE"; //128 char

    // Retrieve Users Object from JWT token
// Retrieve Users Object from JWT token
    public User getUserObject(String token) {
        String username = getClaimFromToken(token, Claims::getSubject);
        Long hospitalId = getClaimFromToken(token, claims -> claims.get("hospitalId", Long.class));
        Long employeeId = getClaimFromToken(token, claims -> claims.get("employeeId", Long.class));
        Long userId = getClaimFromToken(token, claims -> claims.get("userId", Long.class));

        User user = userRepo.findByUserName(username);
        if (user != null) {
            user.setHospital(hospitalId != null ? masHospitalRepository.findById(hospitalId).orElse(null) : null);
            user.setEmployee(employeeId != null ? masEmployeeRepository.findById(employeeId).orElse(null) : null);
            user.setUserId(userId);
        }
        return user;
    }


    // Retrieve username from JWT token
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    // Retrieve expiration date from JWT token
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    // Retrieve all claims from token using the secret key
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    public long getExpirationTime(String token) {
        Date expirationDate = getExpirationDateFromToken(token);
        return expirationDate.getTime();
    }

    // Check if the token has expired
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    // Generate access token for user
//    public String generateAccessToken(UserDetails userDetails) {
//        Map<String, Object> claims = new HashMap<>();
//        return doGenerateToken(claims, userDetails.getUsername(), JWT_TOKEN_VALIDITY);
//    }

    // Generate access token for user with additional details
    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("hospitalId", user.getHospital() != null ? user.getHospital().getId() : null);
        claims.put("employeeId", user.getEmployee() != null ? user.getEmployee().getEmployeeId() : null);
        claims.put("userId", user.getUserId());

        return doGenerateToken(claims, user.getUsername(), JWT_TOKEN_VALIDITY);
    }

    // Generate refresh token for user
    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("hospitalId", user.getHospital() != null ? user.getHospital().getId() : null);
        claims.put("employeeId", user.getEmployee() != null ? user.getEmployee().getEmployeeId() : null);
        claims.put("userId", user.getUserId());

        return doGenerateToken(claims, user.getUsername(), REFRESH_TOKEN_VALIDITY);
    }


    // Generate refresh token for user
//    public String generateRefreshToken(UserDetails userDetails) {
//        Map<String, Object> claims = new HashMap<>();
//        return doGenerateToken(claims, userDetails.getUsername(), REFRESH_TOKEN_VALIDITY);
//    }

    // Common method to generate token
    private String doGenerateToken(Map<String, Object> claims, String subject, long validity) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + validity * 1000))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    // Validate token
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
            Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token);
            final String username = getUsernameFromToken(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // Extract token from request header
    public String getTokenFromHeader() {
        String tokenWithoutBearer = "";
        String token = request.getHeader("Authorization");
        if (token == null) {
            throw new SDDException(HttpStatus.UNAUTHORIZED.value(), "INVALID TOKEN. LOGIN AGAIN.IN-001");
        }
        if (!token.contains("Bearer")) {
            throw new SDDException(HttpStatus.UNAUTHORIZED.value(), "INVALID TOKEN. LOGIN AGAIN.IN-002");
        }
        try {
            String[] tokenWithBearer = token.split(" ");
            tokenWithoutBearer = tokenWithBearer[1];
        } catch (Exception e) {
            throw new SDDException(HttpStatus.UNAUTHORIZED.value(), "INVALID TOKEN. LOGIN AGAIN.IN-03");
        }
        return tokenWithoutBearer;
    }

    public TokenWithExpiry generateAccessTokenWithExpiry(User user, Long departmentId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("hospitalId", user.getHospital() != null ? user.getHospital().getId() : null);
        claims.put("employeeId", user.getEmployee() != null ? user.getEmployee().getEmployeeId() : null);
        claims.put("userId", user.getUserId());
        claims.put("departmentId", departmentId);

        long currentTimeMillis = System.currentTimeMillis();
        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(currentTimeMillis))
                .setExpiration(new Date(currentTimeMillis + JWT_TOKEN_VALIDITY * 1000))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();

        return new TokenWithExpiry(token, currentTimeMillis + JWT_TOKEN_VALIDITY * 1000);
    }


    public TokenWithExpiry generateRefreshTokenWithExpiry(User user, Long departmentId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("hospitalId", user.getHospital() != null ? user.getHospital().getId() : null);
        claims.put("employeeId", user.getEmployee() != null ? user.getEmployee().getEmployeeId() : null);
        claims.put("userId", user.getUserId());
        claims.put("departmentId", departmentId);

        long currentTimeMillis = System.currentTimeMillis();
        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(currentTimeMillis))
                .setExpiration(new Date(currentTimeMillis + REFRESH_TOKEN_VALIDITY * 1000))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();

        return new TokenWithExpiry(token, currentTimeMillis + REFRESH_TOKEN_VALIDITY * 1000);
    }

//for mobile scection to generate token.......
    // 🔹 Generate Token for mobile
    public static String mobileGenerateToken(String mobileNo, Long patientId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("mobileNo", mobileNo);
        claims.put("patientId", patientId);
        return mobiledoGenerateToken(claims,"mobile", JWT_TOKEN_VALIDITY);
    }
    private static String mobiledoGenerateToken(Map<String, Object> claims, String mobile, long validity) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(mobile)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + validity * 1000))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }
    // Generate refresh token for user
    public String mobileGenerateRefreshToken(String mobileNo, Long patientId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("mobileNo", mobileNo);
        claims.put("patientId", patientId);
        return mobiledoGenerateToken(claims, "mobile", REFRESH_TOKEN_VALIDITY);
    }

}



