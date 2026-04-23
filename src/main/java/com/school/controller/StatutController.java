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
    @Value("${anthropic.api.key:}") private String apiKey;

    @GetMapping("/statut")
    public ResponseEntity<Map<String,Object>> statut() {
        long total=profRepo.count();
        long repondus=profRepo.findAll().stream().filter(p->"REPONDU".equals(p.getWhatsappStatut())).count();
        return ResponseEntity.ok(Map.of(
            "backend","UP","version","1.0.0",
            "totalProfesseurs",total,"totalEtudiants",etudRepo.count(),
            "totalModules",moduleRepo.count(),"totalDisponibilites",dispoRepo.count(),
            "totalEdts",edtRepo.count(),"profsRepondus",repondus,
            "profsEnAttente",total-repondus,
            "tauxReponse",total>0?Math.round((repondus*100.0)/total):0,
            "iaActive",apiKey!=null&&!apiKey.isBlank()
        ));
    }
}
