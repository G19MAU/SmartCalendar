package com.smartcalender.app.config;

import com.smartcalender.app.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Utility class for managing JSON Web Tokens (JWT) in a Spring Boot application.
 * This class handles the generation, validation, and extraction of data from JWT tokens,
 * which serve as the backbone of stateless authentication and authorization.
 * It integrates seamlessly with Spring Security's UserDetails to support secure user identification
 * and session-less request handling.
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    private Key key;

    /**
     * Initializes the JWT signing key from the environment variable.
     * This ensures the key is consistent across application restarts,
     * preventing token invalidation and maintaining user sessions.
     */
    @PostConstruct
    private void init() {
        // Decode base64 secret and create signing key
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generates a JWT token for a user based on their UserDetails.
     * The token encapsulates the username as the subject and is signed with a secure key,
     * enabling stateless authentication for subsequent API requests.
     *
     * @param userDetails the UserDetails object containing the username and authorities
     * @return a signed JWT token as a string
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Constructs a JWT token with specified claims and subject (username).
     * This internal method sets the token's issuance and expiration times,
     * signs it with the application's secure key, and ensures time-bound validity for enhanced security.
     *
     * @param claims  additional claims to include in the token (e.g., roles or metadata)
     * @param subject the username to embed as the token's subject
     * @return the fully constructed JWT token as a string
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(key)
                .compact();
    }

    /**
     * Validates a JWT token by checking its username and expiration status.
     * This method ensures the token matches the provided UserDetails and hasn’t expired,
     * forming a critical step in securing API endpoints against unauthorized access.
     *
     * @param token       the JWT token to validate
     * @param userDetails the UserDetails to compare against the token's subject
     * @return true if the token is valid and unexpired, false otherwise
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * Extracts the username from a JWT token’s subject claim.
     * This utility method retrieves the user identifier embedded in the token,
     * facilitating user-specific authentication checks.
     *
     * @param token the JWT token
     * @return the username stored in the token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Retrieves the expiration date from a JWT token.
     * This method supports expiration checks to ensure tokens remain valid only for their intended duration.
     *
     * @param token the JWT token
     * @return the expiration Date of the token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts a specific claim from a JWT token using a provided resolver function.
     * This generic method optimizes token parsing by allowing flexible claim retrieval,
     * supporting extensibility for future claim-based features.
     *
     * @param token          the JWT token
     * @param claimsResolver a function to extract the desired claim from the token’s claims
     * @param <T>            the type of the claim to extract
     * @return the extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parses a JWT token to retrieve all embedded claims.
     * This method verifies the token’s signature using the secure key,
     * ensuring its integrity and authenticity before exposing its payload.
     *
     * @param token the JWT token
     * @return all claims contained within the token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    /**
     * Checks if a JWT token has expired by comparing its expiration claim to the current time.
     * This method enforces time-based security by rejecting expired tokens.
     *
     * @param token the JWT token
     * @return true if the token is expired, false otherwise
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}