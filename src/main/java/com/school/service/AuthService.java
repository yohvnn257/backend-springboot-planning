package com.school.service;
import com.school.config.JwtUtil;
import com.school.entity.Utilisateur;
import com.school.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    /** Change le mot de passe et passe must_change_password a false. */
    @Transactional
    public Map<String,Object> changePassword(String username, String currentPwd, String newPwd) {
        Optional<Utilisateur> opt = utilisateurRepo.findByUsername(username);
        if (opt.isEmpty()) return Map.of("succes", false, "message", "Utilisateur introuvable.");
        Utilisateur u = opt.get();
        if (!passwordEncoder.matches(currentPwd, u.getPasswordHash()))
            return Map.of("succes", false, "message", "Mot de passe actuel incorrect.");
        if (newPwd == null || newPwd.length() < 8)
            return Map.of("succes", false, "message", "Le nouveau mot de passe doit faire au moins 8 caracteres.");
        u.setPasswordHash(passwordEncoder.encode(newPwd));
        u.setMustChangePassword(false);
        utilisateurRepo.save(u);
        log.info("Mot de passe change pour username={}", username);
        return Map.of("succes", true, "message", "Mot de passe mis a jour.");
    }

    /** Indique si le compte par defaut existe et utilise encore son mot de passe d'origine. */
    public Map<String,Object> defaultPasswordInfo() {
        Optional<Utilisateur> opt = utilisateurRepo.findByUsername("secretaire");
        boolean mustChange = opt.map(Utilisateur::getMustChangePassword).orElse(false);
        Map<String,Object> r = new HashMap<>();
        r.put("mustChange", mustChange);
        r.put("defaultUsername", mustChange ? "secretaire" : null);
        r.put("defaultPassword", mustChange ? "secretaire2026" : null);
        return r;
    }
}
