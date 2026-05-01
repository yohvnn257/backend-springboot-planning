package com.school.controller;
import com.school.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api") @RequiredArgsConstructor
public class StatutController {
    private final ProfesseurRepository profRepo;
    private final EtudiantRepository etudRepo;
    private final ModuleRepository moduleRepo;
    private final DisponibiliteRepository dispoRepo;
    private final EmploiDuTempsRepository edtRepo;
    @Value("${gemini.api.key:}") private String apiKey;

    @GetMapping("/statut")
    public ResponseEntity<Map<String,Object>> statut() {
        long total=profRepo.count();
        long repondus=profRepo.findAll().stream().filter(p->"REPONDU".equals(p.getWhatsappStatut())).count();
        return ResponseEntity.ok(Map.ofEntries(
            Map.entry("backend","UP"),
            Map.entry("version","1.0.0"),
            Map.entry("totalProfesseurs",total),
            Map.entry("totalEtudiants",etudRepo.count()),
            Map.entry("totalModules",moduleRepo.count()),
            Map.entry("totalDisponibilites",dispoRepo.count()),
            Map.entry("totalEdts",edtRepo.count()),
            Map.entry("profsRepondus",repondus),
            Map.entry("profsEnAttente",total-repondus),
            Map.entry("tauxReponse",total>0?Math.round((repondus*100.0)/total):0),
            Map.entry("iaActive",apiKey!=null&&!apiKey.isBlank()),
            Map.entry("iaProvider","Gemini 3 Flash")
        ));
    }
}
