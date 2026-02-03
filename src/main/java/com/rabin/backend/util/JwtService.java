package com.rabin.backend.util;

import com.rabin.backend.config.properties.JwtProperties;
import com.rabin.backend.model.User;
import com.rabin.backend.service.auth.TokenBlacklistService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final Key key;
    private final long expiration;
    private final TokenBlacklistService tokenBlacklistService;

    public JwtService(JwtProperties jwtProperties, @Lazy TokenBlacklistService tokenBlacklistService) {
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
        this.expiration = jwtProperties.getExpiration();
        this.tokenBlacklistService = tokenBlacklistService;
    }

    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .addClaims(Map.of(
                        "email", user.getEmail(),
                        "roles", user.getRoles().stream()
                                .map(r -> r.getName())
                                .toList()
                ))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Long extractUserId(String token) {
        return Long.parseLong(
                parseClaims(token).getSubject()
        );
    }

    public boolean validateToken(String token) {
        try {
            // Check if token is blacklisted (logged out)
            if (tokenBlacklistService.isBlacklisted(token)) {
                return false;
            }
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    /**
     * Get the expiration time of a token in milliseconds
     */
    public long getExpirationTime(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().getTime();
        } catch (JwtException | IllegalArgumentException ex) {
            // Return current time + default expiration if can't parse
            return System.currentTimeMillis() + expiration;
        }
    }

    /**
     * Invalidate a token (for logout)
     */
    public void invalidateToken(String token) {
        long expirationTime = getExpirationTime(token);
        tokenBlacklistService.blacklistToken(token, expirationTime);
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
