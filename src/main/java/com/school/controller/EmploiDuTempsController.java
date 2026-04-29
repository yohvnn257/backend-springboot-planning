package com.school.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.dto.request.GenererEdtRequest;
import com.school.entity.EmploiDuTemps;
import com.school.repository.EmploiDuTempsRepository;
import com.school.service.EmploiDuTempsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;
@RestController @RequestMapping("/api") @RequiredArgsConstructor
public class EmploiDuTempsController {
    private final EmploiDuTempsService svc;
    private final EmploiDuTempsRepository edtRepo;
    private final ObjectMapper objectMapper;
    
    @GetMapping("/emplois-du-temps") public ResponseEntity<List<EmploiDuTemps>> getAll(){return ResponseEntity.ok(svc.findAll());}
    @PostMapping("/generer-emploi-du-temps") public ResponseEntity<Map<String,Object>> generer(@RequestBody GenererEdtRequest r){return ResponseEntity.ok(svc.generer(r.getFiliere(),r.getNiveau(),r.getSemaineDu(),r.getSemaineAu()));}
    @PutMapping("/emplois-du-temps/{id}") public ResponseEntity<EmploiDuTemps> update(@PathVariable Long id,@RequestBody Map<String,Object> b){return ResponseEntity.ok(svc.update(id,b));}
    @PostMapping("/envoyer-emploi-du-temps") public ResponseEntity<Map<String,Object>> envoyer(@RequestBody Map<String,Object> b){return ResponseEntity.ok(svc.envoyer(((Number)b.get("emploiDuTempsId")).longValue()));}
    
    @PostMapping("/emploi-du-temps/sauvegarder")
    public ResponseEntity<Map<String,Object>> sauvegarderEdtN8n(@RequestBody Map<String,Object> body){
        try{
            String filiere=(String)body.get("filiere");String niveau=(String)body.get("niveau");
            String semaineDu=(String)body.get("semaineDu");String semaineAu=(String)body.get("semaineAu");
            @SuppressWarnings("unchecked")List<Map<String,Object>> creneaux=(List<Map<String,Object>>)body.get("creneaux");
            if(filiere==null||niveau==null||creneaux==null||creneaux.isEmpty())
                return ResponseEntity.badRequest().body(Map.of("erreur","filiere, niveau, et creneaux requis"));
            EmploiDuTemps edt=EmploiDuTemps.builder().filiere(filiere).niveau(niveau).semaineDu(LocalDate.parse(semaineDu))
                .semaineAu(LocalDate.parse(semaineAu)).creneauxJson(objectMapper.writeValueAsString(creneaux)).statut("GENERE").build();
            EmploiDuTemps saved=edtRepo.save(edt);
            return ResponseEntity.ok(Map.of("id",saved.getId(),"message","EDT sauvegardé par n8n","filiere",filiere,"niveau",niveau));
        }catch(Exception e){
            return ResponseEntity.status(500).body(Map.of("erreur","Erreur sauvegarde: "+e.getMessage()));
        }
    }
}
