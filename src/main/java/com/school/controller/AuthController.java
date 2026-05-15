package com.school.controller;
import com.school.dto.request.ChangePasswordRequest;
import com.school.dto.request.LoginRequest;
import com.school.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/api/auth") @RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Map<String,Object>> login(@RequestBody LoginRequest req) {
        Map<String,Object> r = authService.login(req.getUsername(), req.getPassword());
        return Boolean.TRUE.equals(r.get("succes")) ? ResponseEntity.ok(r) : ResponseEntity.status(401).body(r);
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String,Object>> changePassword(@RequestBody ChangePasswordRequest req) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = principal != null ? principal.toString() : null;
        if (username == null || username.isBlank())
            return ResponseEntity.status(401).body(Map.of("succes", false, "message", "Non authentifie."));
        Map<String,Object> r = authService.changePassword(username, req.getCurrentPassword(), req.getNewPassword());
        return Boolean.TRUE.equals(r.get("succes")) ? ResponseEntity.ok(r) : ResponseEntity.badRequest().body(r);
    }

    @GetMapping("/default-password-info")
    public ResponseEntity<Map<String,Object>> defaultPasswordInfo() {
        return ResponseEntity.ok(authService.defaultPasswordInfo());
    }
}
