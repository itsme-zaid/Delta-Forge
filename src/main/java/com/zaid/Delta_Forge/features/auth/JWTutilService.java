package com.zaid.Delta_Forge.features.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Service
public class JWTutilService {
    private static final String SECRET = "mysecretkeymysecretkeymysecretkey";

    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    public String generateToken(CustomUserDetails customUserDetails) {
        String userId = customUserDetails.getUserId().toString();
        String username = customUserDetails.getUsername();
        return Jwts.builder()
                .subject(userId)
                .claim("username",username)
                .claim("roles",customUserDetails.getAuthorities())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(getSignKey())
                .compact();
    }

    public String extractUserId(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }
    public String extractUsername(String token){
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("username", String.class);
    }
    public List<String> extractRoles(String token){
        var claims = Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        List<?> raw = claims.get("roles", List.class);
        return raw.stream()
                .map(Object::toString)
                .toList();
    }
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSignKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
        /*
        public boolean validateToken(String token, int userVersion) {
            try {
                var claims = Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
            int ver = claims.get("version");
            if(userVersion != ver) return false;
                return true;
            } catch (Exception e) {
                return false;
            }
        }*/
}
