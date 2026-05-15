package com.school.service;
import com.school.entity.Disponibilite;
import com.school.entity.Professeur;
import com.school.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;
import java.util.*;

@Service @RequiredArgsConstructor @Slf4j
public class EmailService {
    private final ProfesseurRepository profRepo;
    private final DisponibiliteRepository dispoRepo;
    private final ModuleRepository moduleRepo;
    private final RestTemplate restTemplate;
    @Value("${n8n.email.trigger.url}") private String n8nUrl;
    @Value("${app.frontend.url}") private String frontendUrl;

    /** Supprime les slashes finaux pour eviter les URLs avec // (cas APP_FRONTEND_URL avec / final). */
    private String frontendUrlNormalise() {
        return frontendUrl == null ? "" : frontendUrl.replaceAll("/+$", "");
    }

    @Transactional(readOnly=true)
    public List<Map<String,Object>> getProfesseursStatuts() {
        return profRepo.findAll().stream().filter(p->p.getEmail()!=null&&!p.getEmail().isBlank()).map(p->{
            Map<String,Object> m=new LinkedHashMap<>();
            m.put("id",p.getId()); m.put("nom",p.getNom());
            m.put("email",p.getEmail());
            m.put("whatsappNumero",p.getTelephone());
            m.put("whatsappStatut",Optional.ofNullable(p.getWhatsappStatut()).orElse("ATTENTE"));
            m.put("telephone", normaliserTelephone(p.getTelephone()));
            m.put("lienReponse",
                p.getResponseToken() != null
                ? frontendUrlNormalise() + "/repondre/" + p.getResponseToken()
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
    // Token idempotent : ne régénère que si manquant, expiré, ou si le prof a déjà répondu.
    // Évite d'invalider les liens déjà envoyés en cas de double-clic ou de double-appel
    // (trigger-bot puis /preparer-envoi via n8n).
    @Transactional
    public List<Map<String,Object>> preparerTokens() {
        List<Professeur> profs = profRepo.findAll();
        LocalDateTime now = LocalDateTime.now();
        profs.forEach(p->{
            boolean tokenInvalide = p.getResponseToken()==null
                || p.getTokenExpireAt()==null
                || p.getTokenExpireAt().isBefore(now)
                || "REPONDU".equals(p.getWhatsappStatut());
            if (tokenInvalide) {
                p.setResponseToken(UUID.randomUUID().toString().replace("-",""));
                p.setTokenExpireAt(now.plusDays(7));
            }
            p.setWhatsappStatut("ATTENTE");
        });
        profRepo.saveAll(profs);
        return profs.stream().filter(p->p.getEmail()!=null&&!p.getEmail().isBlank()).map(p->{
            List<String> mods=moduleRepo.findNomsByProfesseurId(p.getId());
            Map<String,Object> m=new LinkedHashMap<>();
            m.put("id",p.getId()); m.put("nom",p.getNom());
            m.put("matiere", mods.isEmpty() ? "(aucun module assigné)" : String.join(", ", mods));
            m.put("modules",mods); m.put("email",p.getEmail());
            m.put("telephone",p.getTelephone());
            m.put("lienReponse",frontendUrlNormalise()+"/repondre/"+p.getResponseToken());
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
    public void resetStatuts() {
        profRepo.findAll().forEach(p->p.setWhatsappStatut("ATTENTE"));
        log.info("Statuts réinitialisés");
    }
    private String normaliserTelephone(String tel) {
    if (tel == null || tel.isBlank()) return "";

    // Supprimer espaces, tirets, parenthèses
    String clean = tel.replaceAll("[\\s\\-().]+", "").trim();

    // Cas stocké avec +225 : +2250XXXXXXXXX (14 chiffres)
    // Le 0 après +225 est le préfixe national → on le supprime
    // +2250142807548 → +22542807548 (supprime juste le 0)
    if (clean.startsWith("+2250") && clean.length() == 14) {
        return "+225" + clean.substring(5); // saute le 0
    }

    // Déjà bon format +225XXXXXXXXX (12 ou 13 chiffres sans 0)
    if (clean.startsWith("+225") && (clean.length() == 12 || clean.length() == 13)) {
        return clean;
    }

    // Format local avec 0 : 0142807548 (10 chiffres)
    if (clean.startsWith("0") && clean.length() == 10) {
        return "+225" + clean.substring(1); // +225 + 9 chiffres
    }

    // Format local sans 0 : 142807548 (9 chiffres) ou 42807548 (8 chiffres)
    if (clean.length() == 9 || clean.length() == 8) {
        return "+225" + clean;
    }

    // Fallback : retourner tel quel
    return clean;
}
}
