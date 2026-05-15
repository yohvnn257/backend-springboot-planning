package com.school.config;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/** Utilitaire JWT : signe et verifie les tokens HS256. Secret depuis APP_JWT_SECRET. */
@Component
public class JwtUtil {
    @Value("${app.jwt.secret:eduschedule-default-secret-CHANGE-IN-PRODUCTION-via-APP_JWT_SECRET-env-var-min-32-chars}")
    private String secret;
    @Value("${app.jwt.expiration-hours:24}") private long expirationHours;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generate(String username) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
            .subject(username)
            .issuedAt(new Date(now))
            .expiration(new Date(now + expirationHours * 3600_000L))
            .signWith(key())
            .compact();
    }

    /** Retourne le username si le token est valide, sinon null. */
    public String verifier(String token) {
        try {
            Claims c = Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload();
            return c.getSubject();
        } catch (Exception e) {
            return null;
        }
    }
}
