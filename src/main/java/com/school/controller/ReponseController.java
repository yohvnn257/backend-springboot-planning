package com.school.controller;
import com.school.entity.Disponibilite;
import com.school.entity.Professeur;
import com.school.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
@RestController @RequestMapping("/api/reponse") @RequiredArgsConstructor @Slf4j
public class ReponseController {
    private final ProfesseurRepository profRepo;
    private final DisponibiliteRepository dispoRepo;
    private final ModuleRepository moduleRepo;
    private static final DateTimeFormatter TIME_FMT=DateTimeFormatter.ofPattern("HH:mm");

    @GetMapping("/verifier/{token}")
    public ResponseEntity<Map<String,Object>> verifier(@PathVariable String token){
        Optional<Professeur> opt=profRepo.findByResponseToken(token);
        if(opt.isEmpty()) return ResponseEntity.status(404).body(Map.of("valide",false,"message","Lien invalide"));
        Professeur p=opt.get();
        if(p.getTokenExpireAt()!=null&&p.getTokenExpireAt().isBefore(LocalDateTime.now()))
            return ResponseEntity.status(410).body(Map.of("valide",false,"message","Lien expiré. Contactez la secrétaire."));
        List<String> mods=moduleRepo.findNomsByProfesseurId(p.getId());
        return ResponseEntity.ok(Map.of("valide",true,"id",p.getId(),"nom",p.getNom(),
            "matiere",mods.isEmpty()?p.getMatiere():String.join(", ",mods),
            "modules",mods,"email",p.getEmail()));
    }

    @PostMapping("/soumettre/{token}")
    public ResponseEntity<Map<String,Object>> soumettre(@PathVariable String token,@RequestBody Map<String,Object> payload){
        Optional<Professeur> opt=profRepo.findByResponseToken(token);
        if(opt.isEmpty()) return ResponseEntity.status(404).body(Map.of("succes",false,"message","Token invalide"));
        Professeur prof=opt.get();
        @SuppressWarnings("unchecked") List<Map<String,Object>> creneaux=(List<Map<String,Object>>)payload.get("creneaux");
        if(creneaux==null||creneaux.isEmpty()) return ResponseEntity.badRequest().body(Map.of("succes",false));
        List<Disponibilite> list=new ArrayList<>();
        for(Map<String,Object> c:creneaux){
            try{list.add(Disponibilite.builder().professeur(prof)
                .filiere((String)c.getOrDefault("filiere","Non spécifiée"))
                .niveau((String)c.getOrDefault("niveau","L3"))
                .salle((String)c.getOrDefault("salle",""))
                .jour(LocalDate.parse((String)c.get("jour")))
                .heureDebut(LocalTime.parse((String)c.get("heureDebut"),TIME_FMT))
                .heureFin(LocalTime.parse((String)c.get("heureFin"),TIME_FMT))
                .statut("DISPONIBLE").build());
            }catch(Exception e){log.error("Erreur créneau: {}",e.getMessage());}
        }
        dispoRepo.saveAll(list);
        prof.setWhatsappStatut("REPONDU");
        profRepo.save(prof);
        return ResponseEntity.ok(Map.of("succes",true,"creneaux",list.size(),"message","Disponibilités enregistrées"));
    }
}
