package com.school.controller;
import com.school.dto.request.EtudiantRequest;
import com.school.dto.response.ApiResponse;
import com.school.entity.Etudiant;
import com.school.service.EtudiantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/etudiants") @RequiredArgsConstructor
public class EtudiantController {
    private final EtudiantService svc;
    @GetMapping public ResponseEntity<List<Etudiant>> getAll(){return ResponseEntity.ok(svc.findAll());}
    @GetMapping("/filtre") public ResponseEntity<List<Etudiant>> filtre(@RequestParam String filiere,@RequestParam String niveau){return ResponseEntity.ok(svc.findByFiliereAndNiveau(filiere,niveau));}
    @PostMapping public ResponseEntity<Etudiant> create(@RequestBody EtudiantRequest r){return ResponseEntity.status(201).body(svc.create(r));}
    @PutMapping("/{id}") public ResponseEntity<Etudiant> update(@PathVariable Long id,@RequestBody EtudiantRequest r){return ResponseEntity.ok(svc.update(id,r));}
    @DeleteMapping("/{id}") public ResponseEntity<ApiResponse> delete(@PathVariable Long id){svc.delete(id);return ResponseEntity.ok(ApiResponse.ok("Supprimé"));}
}
