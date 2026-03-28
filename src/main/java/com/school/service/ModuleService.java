package com.school.service;
import com.school.dto.request.ModuleRequest;
import com.school.entity.Module;
import com.school.entity.Professeur;
import com.school.exception.ResourceNotFoundException;
import com.school.repository.ModuleRepository;
import com.school.repository.ProfesseurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class ModuleService {
    private final ModuleRepository repo;
    private final ProfesseurRepository profRepo;
    private final EmailService emailService;
    public List<Module> findAll(){return repo.findAll();}
    public Module findById(Long id){return repo.findById(id).orElseThrow(()->new ResourceNotFoundException("Module",id));}
    public List<Module> findByProfesseur(Long id){return repo.findByProfesseurId(id);}
    @Transactional
    public Module create(ModuleRequest r){
        Module m=Module.builder().nom(r.getNom()).niveau(r.getNiveau()).nombreHeures(r.getNombreHeures()!=null?r.getNombreHeures():0).salle(r.getSalle()).build();
        if(r.getProfesseurId()!=null) m.setProfesseur(profRepo.findById(r.getProfesseurId()).orElseThrow(()->new ResourceNotFoundException("Professeur",r.getProfesseurId())));
        return repo.save(m);
    }
    @Transactional
    public Module update(Long id, ModuleRequest r){
        Module m=findById(id);m.setNom(r.getNom());m.setNiveau(r.getNiveau());if(r.getNombreHeures()!=null)m.setNombreHeures(r.getNombreHeures());m.setSalle(r.getSalle());
        m.setProfesseur(r.getProfesseurId()!=null?profRepo.findById(r.getProfesseurId()).orElseThrow(()->new ResourceNotFoundException("Professeur",r.getProfesseurId())):null);
        return repo.save(m);
    }
    @Transactional public void delete(Long id){repo.delete(findById(id));}
    @Transactional public Map<String,Object> envoyerEmail(Long id){
        Module m=findById(id);
        if(m.getProfesseur()==null) throw new IllegalArgumentException("Aucun professeur assigné");
        return emailService.triggerBot();
    }
}
