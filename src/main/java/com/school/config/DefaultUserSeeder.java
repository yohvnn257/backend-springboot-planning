package com.school.config;
import com.school.entity.Utilisateur;
import com.school.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Cree le compte secretaire par defaut si la table utilisateurs est vide. */
@Component @RequiredArgsConstructor @Slf4j
public class DefaultUserSeeder implements CommandLineRunner {
    private final UtilisateurRepository repo;
    private final PasswordEncoder encoder;

    @Override
    public void run(String... args) {
        if (repo.count() > 0) return;
        Utilisateur u = Utilisateur.builder()
            .username("secretaire")
            .passwordHash(encoder.encode("secretaire2026"))
            .mustChangePassword(true)
            .build();
        repo.save(u);
        log.info("Utilisateur par defaut cree : secretaire / secretaire2026 (must_change_password=true)");
    }
}
