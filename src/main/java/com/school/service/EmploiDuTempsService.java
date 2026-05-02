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
        // Le prof saisit juste ses créneaux. La secrétaire choisit filière+niveau au moment de la génération.
        // On prend toutes les dispos de la semaine — la secrétaire édite ensuite chaque créneau si besoin.
        List<Disponibilite> dispos=dispoRepo.findByJourBetween(du,au);
        if(dispos.isEmpty()) throw new IllegalArgumentException("Aucune disponibilité trouvée pour cette semaine. Demandez aux professeurs de soumettre leurs créneaux.");
        List<Map<String,Object>> creneaux=new ArrayList<>();
        for(Disponibilite d:dispos){
            // Recupere les modules du prof (page Modules) pour avoir aussi leur salle assignee
            List<com.school.entity.Module> modules = moduleRepo.findByProfesseurId(d.getProfesseur().getId());
            com.school.entity.Module module = modules.isEmpty() ? null : modules.get(0);
            Map<String,Object> c=new LinkedHashMap<>();
            c.put("jour",d.getJour().toString());
            c.put("jourNom",getNomJour(d.getJour().getDayOfWeek().getValue()));
            c.put("heureDebut",d.getHeureDebut().toString());
            c.put("heureFin",d.getHeureFin().toString());
            c.put("module", module != null ? module.getNom() : "(a completer)");
            c.put("professeur",d.getProfesseur().getNom());
            // La salle vient du module (page Modules), pas de la dispo
            c.put("salle", module != null && module.getSalle() != null ? module.getSalle() : "");
            c.put("filiere",filiere);
            c.put("niveau",niveau);
            creneaux.add(c);
        }
        creneaux.sort(Comparator.comparing(c->c.get("jour").toString()+c.get("heureDebut")));
        try{
            EmploiDuTemps edt=edtRepo.save(EmploiDuTemps.builder().filiere(filiere).niveau(niveau).semaineDu(du).semaineAu(au).creneauxJson(objectMapper.writeValueAsString(creneaux)).statut("GENERE").build());
            return Map.of("message","EDT généré","id",edt.getId(),"creneaux",creneaux);
        }catch(Exception e){throw new RuntimeException("Erreur génération: "+e.getMessage());}
    }
    @Transactional
    public EmploiDuTemps update(Long id, Map<String,Object> body){
        EmploiDuTemps edt=edtRepo.findById(id).orElseThrow(()->new ResourceNotFoundException("EDT",id));
        if(body.containsKey("filiere")) edt.setFiliere((String)body.get("filiere"));
        if(body.containsKey("niveau")) edt.setNiveau((String)body.get("niveau"));
        if(body.containsKey("semaineDu")) edt.setSemaineDu(LocalDate.parse((String)body.get("semaineDu")));
        if(body.containsKey("semaineAu")) edt.setSemaineAu(LocalDate.parse((String)body.get("semaineAu")));
        if(body.containsKey("statut")) edt.setStatut((String)body.get("statut"));
        if(body.containsKey("creneauxJson")) edt.setCreneauxJson((String)body.get("creneauxJson"));
        return edtRepo.save(edt);
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
