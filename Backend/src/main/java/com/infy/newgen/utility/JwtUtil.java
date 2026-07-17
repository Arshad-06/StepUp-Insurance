package com.infy.newgen.utility;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
    private final Key key;

    public JwtUtil(Environment environment) {
        String secret = environment.getProperty("Jwt.SECRET");
        if (secret == null) {
            throw new IllegalStateException("JWT Secret Property Missing");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String email, Integer id, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", id);
        claims.put("role", role);
        return createToken(claims, email);
    }

    public String generateResetToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .claim("purpose", "PASSWORD_RESET")
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 5))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 30)) // 30 Minute Expiry
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Boolean validateResetToken(String token, String email) {
        try {
            final String extractedEmail = this.extractAllClaims(token).getSubject();
            final String purpose = this.extractPurpose(token);

            return (extractedEmail.equals(email) &&
                    validateToken(token) &&
                    "PASSWORD_RESET".equals(purpose));
        } catch (Exception e) {
            return false;
        }
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    public String extractPurpose(String token) {
        return this.extractAllClaims(token).get("purpose", String.class);
    }
}