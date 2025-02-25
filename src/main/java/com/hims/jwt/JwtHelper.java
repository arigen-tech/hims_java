package com.hims.jwt;



import com.hims.entity.User;
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
//
//    //requirement :
////    public static final long JWT_TOKEN_VALIDITY = 24 * 155 * 60 * 60; //5 months
//    public static final long JWT_TOKEN_VALIDITY = 30 * 60; // 30 minutes in seconds
//
//    @Autowired
//    private HttpServletRequest request;
//    //    public static final long JWT_TOKEN_VALIDITY =  60;
//    private final String secret = "afafasfafafasfasfasfafacasdasfasxASFACASDFACASDFASFASFDAFASFASDAADSCSDFADCVSGCFVADXCcadwavfsfarvf";
//
//    //retrieve username from jwt token
//    public String getUsernameFromToken(String token) {
//        return getClaimFromToken(token, Claims::getSubject);
//    }
//
//    //retrieve expiration date from jwt token
//    public Date getExpirationDateFromToken(String token) {
//        return getClaimFromToken(token, Claims::getExpiration);
//    }
//
//    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
//        final Claims claims = getAllClaimsFromToken(token);
//        return claimsResolver.apply(claims);
//    }
//
//    //for retrieveing any information from token we will need the secret key
//    private Claims getAllClaimsFromToken(String token) {
//        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
//    }
//
//    public long getExpirationTime(String token) {
//        Date expirationDate = getExpirationDateFromToken(token);
//        return expirationDate.getTime();
//    }
//
//
//    //check if the token has expired
//    private Boolean isTokenExpired(String token) {
//        final Date expiration = getExpirationDateFromToken(token);
//        return expiration.before(new Date());
//    }
//
//    //generate token for user
//    public String generateToken(UserDetails userDetails) {
//        Map<String, Object> claims = new HashMap<>();
//        return doGenerateToken(claims, userDetails.getUsername());
//    }
//
//    //while creating the token -
//    //1. Define  claims of the token, like Issuer, Expiration, Subject, and the ID
//    //2. Sign the JWT using the HS512 algorithm and secret key.
//    //3. According to JWS Compact Serialization(https://tools.ietf.org/html/draft-ietf-jose-json-web-signature-41#section-3.1)
//    //   compaction of the JWT to a URL-safe string
//    private String doGenerateToken(Map<String, Object> claims, String subject) {
//
//        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
//                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
//                .signWith(SignatureAlgorithm.HS512, secret).compact();
//    }
//
//    //validate token
//    public Boolean validateToken(String token, UserDetails userDetails) {
//        final String username = getUsernameFromToken(token);
//        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
//    }
//
//    public String getTokeFromHeader() {
//
//        String tokenWithoutBearer = "";
//        String token = request.getHeader("Authorization");
//        if (token == null) {
//            throw new SDDException(HttpStatus.UNAUTHORIZED.value(), "INVALID TOKEN. LOGIN AGAIN.IN-001");
//        }
//        if (!(token.contains("Bearer"))) {
//            throw new SDDException(HttpStatus.UNAUTHORIZED.value(), "INVALID TOKEN. LOGIN AGAIN.IN-002");
//        }
//        try {
//            String[] tokenWithBearer = token.split(" ");
//            tokenWithoutBearer = tokenWithBearer[1];
//        } catch (Exception e) {
//            throw new SDDException(HttpStatus.UNAUTHORIZED.value(), "INVALID TOKEN. LOGIN AGAIN.IN-03");
//        }
//
//        return tokenWithoutBearer;
//    }






    public static final long JWT_TOKEN_VALIDITY = 7 * 24 * 60 * 60; // 30 minutes in seconds
    public static final long REFRESH_TOKEN_VALIDITY = 7 * 24 * 60 * 60; // 7 days in seconds

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private UserRepo userRepo;

//     private final String secret = "afafasfafafasfasfasfafacasdasfasxASFACASDFACASDFASFASFDAFASFASDAADSCSDFADCVSGCFVADXCcadwavfsfarvf";
   private final String secret = "1KCrT4BFo9EMUNJjQ0y8VswrKFSJmIHp1jZJVP1IU5999EOqb3E1gmNpf5FzYXIZrwpPDHLhRcORigN84ftPfuOt2Q2IKTmRfJP5RRhRCfJJ2wJ4vlMK70fWFeIT5QBE"; //128 char

    // Retrieve Users Object from JWT token
    public User getUserObject(String token) {
        String user= getClaimFromToken(token, Claims::getSubject);
        return userRepo.findByUsername(user);
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
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return doGenerateToken(claims, userDetails.getUsername(), JWT_TOKEN_VALIDITY);
    }

    // Generate refresh token for user
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return doGenerateToken(claims, userDetails.getUsername(), REFRESH_TOKEN_VALIDITY);
    }

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

}
