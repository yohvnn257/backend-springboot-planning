package com.school.controller;

import com.school.repository.DisponibiliteRepository;
import com.school.service.ClaudeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/ia") @RequiredArgsConstructor
public class IaController {
    private final ClaudeService claudeService;
    private final DisponibiliteRepository dispoRepo;
    @Value("${anthropic.api.key:}") private String apiKey;
    @Value("${anthropic.model:claude-sonnet-4-20250514}") private String model;

    @GetMapping("/statut")
    public ResponseEntity<Map<String,Object>> statut() {
        return ResponseEntity.ok(Map.of(
            "actif", apiKey != null && !apiKey.isBlank(),
            "modele", model,
            "message", apiKey != null && !apiKey.isBlank() ? "Claude IA connecté ✅" : "Ajoutez ANTHROPIC_API_KEY dans Render"
        ));
    }

    @PostMapping("/optimiser-edt")
    public ResponseEntity<Map<String,Object>> optimiser(@RequestBody Map<String,Object> body) {
        String filiere = (String) body.getOrDefault("filiere","");
        String niveau = (String) body.getOrDefault("niveau","");
        @SuppressWarnings("unchecked")
        List<Map<String,Object>> creneaux = (List<Map<String,Object>>) body.getOrDefault("creneaux", List.of());
        if (creneaux.isEmpty()) return ResponseEntity.badRequest().body(Map.of("erreur","Aucun créneau fourni"));
        return ResponseEntity.ok(claudeService.optimiserEdt(filiere, niveau, creneaux));
    }

    @PostMapping("/analyser-disponibilites")
    public ResponseEntity<Map<String,Object>> analyser(@RequestBody Map<String,Object> body) {
        String filiere = (String) body.getOrDefault("filiere","");
        String niveau = (String) body.getOrDefault("niveau","");
        @SuppressWarnings("unchecked")
        List<Map<String,Object>> dispos = (List<Map<String,Object>>) body.getOrDefault("disponibilites", List.of());
        return ResponseEntity.ok(claudeService.analyserDisponibilites(filiere, niveau, dispos));
    }
}
