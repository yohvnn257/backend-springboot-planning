package com.school.config;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Date;

/**
 * Utilitaire JWT : signe et verifie les tokens HMAC-SHA. Secret depuis APP_JWT_SECRET.
 * Aucun secret par defaut n'est code en dur : si APP_JWT_SECRET est absent ou trop court
 * (< 32 octets), une cle aleatoire est generee au demarrage. Les tokens sont alors
 * invalides a chaque redemarrage — acceptable en local, a eviter en production.
 */
@Component @Slf4j
public class JwtUtil {
    private static final int MIN_SECRET_BYTES = 32;

    @Value("${app.jwt.secret:}")
    private String secret;
    @Value("${app.jwt.expiration-hours:24}") private long expirationHours;

    private SecretKey key;

    @PostConstruct
    void init() {
        byte[] bytes = secret == null ? new byte[0] : secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < MIN_SECRET_BYTES) {
            if (bytes.length > 0) {
                log.warn("APP_JWT_SECRET trop court ({} octets, minimum {}) : ignore.", bytes.length, MIN_SECRET_BYTES);
            }
            log.warn("APP_JWT_SECRET non defini : cle JWT aleatoire generee. "
                + "Les sessions seront invalidees a chaque redemarrage. Definir APP_JWT_SECRET en production.");
            bytes = new byte[64];
            new SecureRandom().nextBytes(bytes);
        }
        key = Keys.hmacShaKeyFor(bytes);
    }

    public String generate(String username) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
            .subject(username)
            .issuedAt(new Date(now))
            .expiration(new Date(now + expirationHours * 3600_000L))
            .signWith(key)
            .compact();
    }

    /** Retourne le username si le token est valide, sinon null. */
    public String verifier(String token) {
        try {
            Claims c = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
            return c.getSubject();
        } catch (Exception e) {
            return null;
        }
    }
}
