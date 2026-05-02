package com.school.service;
import com.school.dto.request.ProfesseurRequest;
import com.school.entity.Professeur;
import com.school.exception.ResourceNotFoundException;
import com.school.repository.ModuleRepository;
import com.school.repository.ProfesseurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class ProfesseurService {
    private final ProfesseurRepository repo;
    private final ModuleRepository moduleRepo;
    public List<Professeur> findAll(){return repo.findAll();}
    public List<Map<String,Object>> findAllAvecModules(){
        return repo.findAll().stream().map(p->{
            Map<String,Object> m=new LinkedHashMap<>();
            m.put("id",p.getId());m.put("nom",p.getNom());m.put("email",p.getEmail());
            m.put("telephone",p.getTelephone());m.put("whatsappStatut",p.getWhatsappStatut());
            m.put("modules",moduleRepo.findNomsByProfesseurId(p.getId()));
            return m;
        }).toList();
    }
    public Professeur findById(Long id){return repo.findById(id).orElseThrow(()->new ResourceNotFoundException("Professeur",id));}
    @Transactional
    public Professeur create(ProfesseurRequest r){
        if(repo.existsByEmail(r.getEmail())) throw new IllegalArgumentException("Email déjà utilisé: "+r.getEmail());
        return repo.save(Professeur.builder().nom(r.getNom()).email(r.getEmail()).telephone(normaliserTelephone(r.getTelephone())).build());
    }
    @Transactional
    public Professeur update(Long id, ProfesseurRequest r){
        Professeur p=findById(id);p.setNom(r.getNom());
        p.setEmail(r.getEmail());p.setTelephone(normaliserTelephone(r.getTelephone()));return repo.save(p);
    }
    // Normalise vers E.164 (requis par Twilio). Default : Côte d'Ivoire (+225) si numéro local 10 chiffres.
    static String normaliserTelephone(String tel){
        if(tel==null||tel.isBlank()) return tel;
        String t=tel.trim();
        if(t.startsWith("+")) return "+"+t.substring(1).replaceAll("[^0-9]","");
        String chiffres=t.replaceAll("[^0-9]","");
        if(chiffres.length()==10&&chiffres.startsWith("0")) return "+225"+chiffres.substring(1);
        if(chiffres.length()==12&&chiffres.startsWith("225")) return "+"+chiffres;
        if(chiffres.length()==9) return "+225"+chiffres;
        return chiffres.isEmpty()?null:"+"+chiffres;
    }
    @Transactional public void delete(Long id){repo.delete(findById(id));}
    @Transactional public Professeur updateStatut(Long id, String s){Professeur p=findById(id);p.setWhatsappStatut(s);return repo.save(p);}
}
