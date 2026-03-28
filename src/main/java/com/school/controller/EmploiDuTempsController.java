package com.school.controller;
import com.school.dto.request.GenererEdtRequest;
import com.school.entity.EmploiDuTemps;
import com.school.service.EmploiDuTempsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api") @RequiredArgsConstructor
public class EmploiDuTempsController {
    private final EmploiDuTempsService svc;
    @GetMapping("/emplois-du-temps") public ResponseEntity<List<EmploiDuTemps>> getAll(){return ResponseEntity.ok(svc.findAll());}
    @PostMapping("/generer-emploi-du-temps") public ResponseEntity<Map<String,Object>> generer(@RequestBody GenererEdtRequest r){return ResponseEntity.ok(svc.generer(r.getFiliere(),r.getNiveau(),r.getSemaineDu(),r.getSemaineAu()));}
    @PutMapping("/emplois-du-temps/{id}") public ResponseEntity<EmploiDuTemps> update(@PathVariable Long id,@RequestBody Map<String,Object> b){return ResponseEntity.ok(svc.update(id,b));}
    @PostMapping("/envoyer-emploi-du-temps") public ResponseEntity<Map<String,Object>> envoyer(@RequestBody Map<String,Object> b){return ResponseEntity.ok(svc.envoyer(((Number)b.get("emploiDuTempsId")).longValue()));}
}
