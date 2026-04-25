package com.school.controller;
import com.school.dto.response.ApiResponse;
import com.school.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api/webhook") @RequiredArgsConstructor
public class WebhookController {
    private final EmailService emailService;
    @GetMapping("/professeurs-whatsapp")
    public ResponseEntity<Map<String,Object>> getStatuts(){
        List<Map<String,Object>> p=emailService.getProfesseursStatuts();
        return ResponseEntity.ok(Map.of("professeurs",p,"total",p.size()));
    }
    @PostMapping("/trigger-bot") public ResponseEntity<Map<String,Object>> trigger(){return ResponseEntity.ok(emailService.triggerBot());}
    @PostMapping("/preparer-envoi")
    public ResponseEntity<Map<String,Object>> preparerEnvoi(){
        List<Map<String,Object>> profs = emailService.preparerTokens();
        return ResponseEntity.ok(Map.of("source","backend","total",profs.size(),"professeurs",profs));
    }
    @PostMapping("/reset-statuts-whatsapp") public ResponseEntity<ApiResponse> reset(){emailService.resetStatuts();return ResponseEntity.ok(ApiResponse.ok("Statuts réinitialisés"));}
    @PostMapping("/disponibilite-whatsapp") public ResponseEntity<Map<String,Object>> recevoir(@RequestBody Map<String,Object> p){return ResponseEntity.ok(emailService.sauvegarderDisponibilites(p));}
}
