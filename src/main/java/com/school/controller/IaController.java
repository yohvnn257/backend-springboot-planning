package com.school.controller;

import com.school.repository.DisponibiliteRepository;
import com.school.service.IaService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/ia") @RequiredArgsConstructor
public class IaController {
    private final IaService iaService;
    private final DisponibiliteRepository dispoRepo;
    @Value("${gemini.api.key:}") private String apiKey;
    @Value("${gemini.model:gemini-2.5-flash}") private String model;

    @GetMapping("/statut")
    public ResponseEntity<Map<String,Object>> statut() {
        return ResponseEntity.ok(Map.of(
            "actif", apiKey != null && !apiKey.isBlank(),
            "provider", "Google Gemini",
            "modele", model,
            "message", apiKey != null && !apiKey.isBlank() ? "Gemini 2.5 Flash connecté ✅" : "Ajoutez GEMINI_API_KEY dans Render"
        ));
    }

    @PostMapping("/optimiser-edt")
    public ResponseEntity<Map<String,Object>> optimiser(@RequestBody Map<String,Object> body) {
        String filiere = (String) body.getOrDefault("filiere","");
        String niveau = (String) body.getOrDefault("niveau","");
        String semaineDu = (String) body.getOrDefault("semaineDu", "");
        String semaineAu = (String) body.getOrDefault("semaineAu", "");
        @SuppressWarnings("unchecked")
        List<Map<String,Object>> creneaux = (List<Map<String,Object>>) body.getOrDefault("creneaux", List.of());
        if (creneaux.isEmpty()) return ResponseEntity.badRequest().body(Map.of("erreur","Aucun créneau fourni"));
        return ResponseEntity.ok(iaService.optimiserEdt(filiere, niveau, semaineDu, semaineAu, creneaux));
    }

    @PostMapping("/analyser-disponibilites")
    public ResponseEntity<Map<String,Object>> analyser(@RequestBody Map<String,Object> body) {
        String filiere = (String) body.getOrDefault("filiere","");
        String niveau = (String) body.getOrDefault("niveau","");
        @SuppressWarnings("unchecked")
        List<Map<String,Object>> dispos = (List<Map<String,Object>>) body.getOrDefault("disponibilites", List.of());
        return ResponseEntity.ok(iaService.analyserDisponibilites(filiere, niveau, dispos));
    }
}
