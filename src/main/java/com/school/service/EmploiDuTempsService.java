package com.school.service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.entity.*;
import com.school.exception.ResourceNotFoundException;
import com.school.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDate;
import java.util.*;
@Service @RequiredArgsConstructor @Slf4j @Transactional(readOnly=true)
public class EmploiDuTempsService {
    private final DisponibiliteRepository dispoRepo;
    private final EmploiDuTempsRepository edtRepo;
    private final EtudiantRepository etudRepo;
    private final ModuleRepository moduleRepo;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    @Value("${n8n.webhook.url}") private String n8nUrl;
    public List<EmploiDuTemps> findAll(){return edtRepo.findAllByOrderByDateCreationDesc();}
    @Transactional
    public Map<String,Object> generer(String filiere,String niveau,LocalDate du,LocalDate au){
        List<Disponibilite> dispos=dispoRepo.findByFiliereAndNiveauAndJourBetween(filiere,niveau,du,au);
        if(dispos.isEmpty()) throw new IllegalArgumentException("Aucune disponibilité pour "+filiere+" "+niveau);
        List<Map<String,Object>> creneaux=new ArrayList<>();
        for(Disponibilite d:dispos){
            List<String> mods=moduleRepo.findNomsByProfesseurId(d.getProfesseur().getId());
            Map<String,Object> c=new LinkedHashMap<>();
            c.put("jour",d.getJour().toString());
            c.put("jourNom",getNomJour(d.getJour().getDayOfWeek().getValue()));
            c.put("heureDebut",d.getHeureDebut().toString());
            c.put("heureFin",d.getHeureFin().toString());
            c.put("matiere",mods.isEmpty()?d.getProfesseur().getMatiere():mods.get(0));
            c.put("professeur",d.getProfesseur().getNom());
            c.put("salle",d.getSalle()!=null?d.getSalle():"");
            creneaux.add(c);
        }
        creneaux.sort(Comparator.comparing(c->c.get("jour").toString()+c.get("heureDebut")));
        try{
            EmploiDuTemps edt=edtRepo.save(EmploiDuTemps.builder().filiere(filiere).niveau(niveau).semaineDu(du).semaineAu(au).creneauxJson(objectMapper.writeValueAsString(creneaux)).statut("GENERE").build());
            return Map.of("message","EDT généré","id",edt.getId(),"creneaux",creneaux);
        }catch(Exception e){throw new RuntimeException("Erreur génération: "+e.getMessage());}
    }
    @Transactional
    public Map<String,Object> envoyer(Long id){
        EmploiDuTemps edt=edtRepo.findById(id).orElseThrow(()->new ResourceNotFoundException("EDT",id));
        List<String> emails=etudRepo.findByFiliereAndNiveau(edt.getFiliere(),edt.getNiveau()).stream().map(Etudiant::getEmail).toList();
        try{
            Map<String,Object> payload=new LinkedHashMap<>();
            payload.put("emploiDuTempsId",edt.getId());payload.put("filiere",edt.getFiliere());payload.put("niveau",edt.getNiveau());
            payload.put("semaineDu",edt.getSemaineDu().toString());payload.put("semaineAu",edt.getSemaineAu().toString());
            payload.put("creneaux",objectMapper.readValue(edt.getCreneauxJson(),List.class));payload.put("destinataires",emails);
            HttpHeaders h=new HttpHeaders();h.setContentType(MediaType.APPLICATION_JSON);
            restTemplate.postForEntity(n8nUrl,new HttpEntity<>(payload,h),String.class);
            edt.setStatut("ENVOYE");edtRepo.save(edt);
            return Map.of("message","Envoyé à "+emails.size()+" étudiant(s)","emails",emails);
        }catch(Exception e){throw new RuntimeException("Erreur envoi: "+e.getMessage());}
    }
    private String getNomJour(int d){return switch(d){case 1->"Lundi";case 2->"Mardi";case 3->"Mercredi";case 4->"Jeudi";case 5->"Vendredi";default->"Autre";};}
}
