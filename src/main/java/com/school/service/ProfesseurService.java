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
        return repo.save(Professeur.builder().nom(r.getNom()).matiere(r.getMatiere()!=null?r.getMatiere():"").email(r.getEmail()).telephone(r.getTelephone()).build());
    }
    @Transactional
    public Professeur update(Long id, ProfesseurRequest r){
        Professeur p=findById(id);p.setNom(r.getNom());if(r.getMatiere()!=null)p.setMatiere(r.getMatiere());
        p.setEmail(r.getEmail());p.setTelephone(r.getTelephone());return repo.save(p);
    }
    @Transactional public void delete(Long id){repo.delete(findById(id));}
    @Transactional public Professeur updateStatut(Long id, String s){Professeur p=findById(id);p.setWhatsappStatut(s);return repo.save(p);}
}
