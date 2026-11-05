package com.datamint.twin.security;

import com.datamint.twin.model.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey key;
    private final long expiryMs;

    public JwtService(@Value("${twin.jwt.secret}") String secret,
                      @Value("${twin.jwt.expiry-minutes}") long minutes) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiryMs = minutes * 60_000L;
    }

    public String create(AppUser u) {
        Date now = new Date();
        return Jwts.builder().subject(u.getUsername()).claim("role", u.getRole())
                .issuedAt(now).expiration(new Date(now.getTime() + expiryMs)).signWith(key).compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
