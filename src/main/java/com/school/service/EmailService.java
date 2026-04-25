package com.school.service;
import com.school.entity.Disponibilite;
import com.school.entity.Professeur;
import com.school.exception.ResourceNotFoundException;
import com.school.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service @RequiredArgsConstructor @Slf4j
public class EmailService {
    private final ProfesseurRepository profRepo;
    private final DisponibiliteRepository dispoRepo;
    private final ModuleRepository moduleRepo;
    private final RestTemplate restTemplate;
    @Value("${n8n.email.trigger.url}") private String n8nUrl;
    @Value("${app.frontend.url}") private String frontendUrl;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    @Transactional(readOnly=true)
    public List<Map<String,Object>> getProfesseursStatuts() {
        return profRepo.findAll().stream().filter(p->p.getEmail()!=null&&!p.getEmail().isBlank()).map(p->{
            Map<String,Object> m=new LinkedHashMap<>();
            m.put("id",p.getId()); m.put("nom",p.getNom());
            m.put("matiere",p.getMatiere());
            m.put("email",p.getEmail());
            m.put("whatsappNumero",p.getTelephone());
            m.put("whatsappStatut",Optional.ofNullable(p.getWhatsappStatut()).orElse("ATTENTE"));
            m.put("telephone", p.getTelephone());
            m.put("lienReponse",
                p.getResponseToken() != null
                ? frontendUrl + "/repondre/" + p.getResponseToken()
                : null
            );
            m.put("modules",moduleRepo.findNomsByProfesseurId(p.getId()));
            List<Disponibilite> d=dispoRepo.findByProfesseurId(p.getId());
            if(!d.isEmpty()){m.put("filiere",d.get(d.size()-1).getFiliere());m.put("niveau",d.get(d.size()-1).getNiveau());}
            else{m.put("filiere","");m.put("niveau","");}
            return m;
        }).toList();
    }

    // Étape 1 : sauvegarde DB (transaction courte). Public pour appel direct depuis n8n.
    @Transactional
    public List<Map<String,Object>> preparerTokens() {
        List<Professeur> profs = profRepo.findAll();
        profs.forEach(p->{
            p.setWhatsappStatut("ATTENTE");
            p.setResponseToken(UUID.randomUUID().toString().replace("-",""));
            p.setTokenExpireAt(LocalDateTime.now().plusDays(7));
        });
        profRepo.saveAll(profs);
        return profs.stream().filter(p->p.getEmail()!=null&&!p.getEmail().isBlank()).map(p->{
            List<String> mods=moduleRepo.findNomsByProfesseurId(p.getId());
            Map<String,Object> m=new LinkedHashMap<>();
            m.put("id",p.getId()); m.put("nom",p.getNom());
            m.put("matiere",mods.isEmpty()?p.getMatiere():String.join(", ",mods));
            m.put("modules",mods); m.put("email",p.getEmail());
            m.put("telephone",p.getTelephone());
            m.put("lienReponse",frontendUrl+"/repondre/"+p.getResponseToken());
            return m;
        }).toList();
    }

    // Étape 2 : appel n8n hors transaction
    public Map<String,Object> triggerBot() {
        log.info("Déclenchement bot email");
        List<Map<String,Object>> profs = preparerTokens();
        try {
            HttpHeaders h=new HttpHeaders(); h.setContentType(MediaType.APPLICATION_JSON);
            Map<String,Object> payload=Map.of("source","dashboard","timestamp",LocalDateTime.now().toString(),"professeurs",profs);
            ResponseEntity<String> resp=restTemplate.postForEntity(n8nUrl,new HttpEntity<>(payload,h),String.class);
            log.info("Bot déclenché - status: {}",resp.getStatusCode());
            return Map.of("succes",true,"message","Emails envoyés — "+profs.size()+" professeurs","professeurs",profs.size());
        } catch(Exception e) {
            log.error("Erreur n8n: {}",e.getMessage());
            return Map.of("succes",false,"message","Tokens générés, n8n indisponible: "+e.getMessage(),"professeurs",profs.size());
        }
    }

    @Transactional
    public Map<String,Object> sauvegarderDisponibilites(Map<String,Object> payload) {
        String token=(String)payload.get("token");
        String emailProf=(String)payload.get("emailProfesseur");
        Professeur prof = token!=null&&!token.isBlank()
            ? profRepo.findByResponseToken(token).orElseThrow(()->new ResourceNotFoundException("Token invalide"))
            : profRepo.findByEmail(emailProf).orElseThrow(()->new ResourceNotFoundException("Prof non trouvé: "+emailProf));
        @SuppressWarnings("unchecked") List<Map<String,Object>> creneaux=(List<Map<String,Object>>)payload.get("creneaux");
        if(creneaux==null||creneaux.isEmpty()) throw new IllegalArgumentException("Aucun créneau");
        List<Disponibilite> list=new ArrayList<>();
        for(Map<String,Object> c:creneaux) {
            list.add(Disponibilite.builder().professeur(prof)
                .filiere((String)c.getOrDefault("filiere","Non spécifiée"))
                .niveau((String)c.getOrDefault("niveau","L3"))
                .salle((String)c.getOrDefault("salle",""))
                .jour(LocalDate.parse((String)c.get("jour"),DATE_FMT))
                .heureDebut(LocalTime.parse((String)c.get("heureDebut"),TIME_FMT))
                .heureFin(LocalTime.parse((String)c.get("heureFin"),TIME_FMT))
                .statut("DISPONIBLE").build());
        }
        dispoRepo.saveAll(list);
        prof.setWhatsappStatut("REPONDU");
        profRepo.save(prof);
        return Map.of("succes",true,"professeur",prof.getNom(),"creneaux",list.size());
    }

    @Transactional
    public void resetStatuts() {
        profRepo.findAll().forEach(p->p.setWhatsappStatut("ATTENTE"));
        log.info("Statuts réinitialisés");
    }
}
