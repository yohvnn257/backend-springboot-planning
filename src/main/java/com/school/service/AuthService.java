package com.school.service;
import com.school.config.JwtUtil;
import com.school.entity.Utilisateur;
import com.school.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service @RequiredArgsConstructor @Slf4j
public class AuthService {
    private final UtilisateurRepository utilisateurRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /** Verifie credentials et retourne un JWT + indication "doit changer le mdp" si applicable. */
    public Map<String,Object> login(String username, String password) {
        Optional<Utilisateur> opt = utilisateurRepo.findByUsername(username);
        if (opt.isEmpty() || !passwordEncoder.matches(password, opt.get().getPasswordHash())) {
            log.info("Echec de connexion pour username={}", username);
            return Map.of("succes", false, "message", "Identifiants incorrects.");
        }
        Utilisateur u = opt.get();
        String token = jwtUtil.generate(u.getUsername());
        Map<String,Object> r = new HashMap<>();
        r.put("succes", true);
        r.put("token", token);
        r.put("username", u.getUsername());
        r.put("mustChangePassword", u.getMustChangePassword());
        return r;
    }

    /**
     * Identifiants de connexion affiches sur la page de login.
     * Le mot de passe est FIXE et ne se change qu'en base (table utilisateurs) :
     * il n'existe volontairement plus d'endpoint pour le modifier via l'application.
     * NB : si tu changes le hash en base, mets aussi a jour la constante ci-dessous.
     */
    public Map<String,Object> defaultPasswordInfo() {
        Map<String,Object> r = new HashMap<>();
        r.put("mustChange", false);
        r.put("defaultUsername", "secretaire");
        r.put("defaultPassword", "secretaire2026");
        return r;
    }
}
