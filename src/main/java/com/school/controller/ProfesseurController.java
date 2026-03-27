package com.school.controller;
import com.school.dto.request.ProfesseurRequest;
import com.school.dto.response.ApiResponse;
import com.school.entity.Professeur;
import com.school.service.ProfesseurService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api/professeurs") @RequiredArgsConstructor
public class ProfesseurController {
    private final ProfesseurService svc;
    @GetMapping public ResponseEntity<List<Professeur>> getAll(){return ResponseEntity.ok(svc.findAll());}
    @GetMapping("/avec-modules") public ResponseEntity<List<Map<String,Object>>> avecModules(){return ResponseEntity.ok(svc.findAllAvecModules());}
    @GetMapping("/{id}") public ResponseEntity<Professeur> getById(@PathVariable Long id){return ResponseEntity.ok(svc.findById(id));}
    @PostMapping public ResponseEntity<Professeur> create(@RequestBody ProfesseurRequest r){return ResponseEntity.status(201).body(svc.create(r));}
    @PutMapping("/{id}") public ResponseEntity<Professeur> update(@PathVariable Long id,@RequestBody ProfesseurRequest r){return ResponseEntity.ok(svc.update(id,r));}
    @DeleteMapping("/{id}") public ResponseEntity<ApiResponse> delete(@PathVariable Long id){svc.delete(id);return ResponseEntity.ok(ApiResponse.ok("Supprimé"));}
    @PatchMapping("/{id}/whatsapp-statut") public ResponseEntity<Professeur> statut(@PathVariable Long id,@RequestBody Map<String,String> b){return ResponseEntity.ok(svc.updateStatut(id,b.get("statut")));}
}
