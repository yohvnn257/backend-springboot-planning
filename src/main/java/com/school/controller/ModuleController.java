package com.school.controller;
import com.school.dto.request.ModuleRequest;
import com.school.dto.response.ApiResponse;
import com.school.entity.Module;
import com.school.service.ModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api/modules") @RequiredArgsConstructor
public class ModuleController {
    private final ModuleService svc;
    @GetMapping public ResponseEntity<List<Module>> getAll(){return ResponseEntity.ok(svc.findAll());}
    @GetMapping("/{id}") public ResponseEntity<Module> getById(@PathVariable Long id){return ResponseEntity.ok(svc.findById(id));}
    @GetMapping("/professeur/{id}") public ResponseEntity<List<Module>> byProf(@PathVariable Long id){return ResponseEntity.ok(svc.findByProfesseur(id));}
    @PostMapping public ResponseEntity<Module> create(@RequestBody ModuleRequest r){return ResponseEntity.status(201).body(svc.create(r));}
    @PutMapping("/{id}") public ResponseEntity<Module> update(@PathVariable Long id,@RequestBody ModuleRequest r){return ResponseEntity.ok(svc.update(id,r));}
    @DeleteMapping("/{id}") public ResponseEntity<ApiResponse> delete(@PathVariable Long id){svc.delete(id);return ResponseEntity.ok(ApiResponse.ok("Supprimé"));}
    @PostMapping("/{id}/envoyer-email") public ResponseEntity<Map<String,Object>> envoyerEmail(@PathVariable Long id){return ResponseEntity.ok(svc.envoyerEmail(id));}
}
