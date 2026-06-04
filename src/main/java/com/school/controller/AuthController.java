package com.school.controller;
import com.school.dto.request.LoginRequest;
import com.school.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    @GetMapping("/default-password-info")
    public ResponseEntity<Map<String,Object>> defaultPasswordInfo() {
        return ResponseEntity.ok(authService.defaultPasswordInfo());
    }
}
