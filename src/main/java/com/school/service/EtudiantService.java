package com.school.service;
import com.school.dto.request.EtudiantRequest;
import com.school.entity.Etudiant;
import com.school.exception.ResourceNotFoundException;
import com.school.repository.EtudiantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class EtudiantService {
    private final EtudiantRepository repo;
    public List<Etudiant> findAll(){return repo.findAll();}
    public Etudiant findById(Long id){return repo.findById(id).orElseThrow(()->new ResourceNotFoundException("Étudiant",id));}
    public List<Etudiant> findByFiliereAndNiveau(String f,String n){return repo.findByFiliereAndNiveau(f,n);}
    @Transactional public Etudiant create(EtudiantRequest r){
        if(repo.existsByEmail(r.getEmail())) throw new IllegalArgumentException("Email déjà utilisé");
        return repo.save(Etudiant.builder().nom(r.getNom()).email(r.getEmail()).filiere(r.getFiliere()).niveau(r.getNiveau()).build());
    }
    @Transactional public Etudiant update(Long id,EtudiantRequest r){Etudiant e=findById(id);e.setNom(r.getNom());e.setEmail(r.getEmail());e.setFiliere(r.getFiliere());e.setNiveau(r.getNiveau());return repo.save(e);}
    @Transactional public void delete(Long id){repo.delete(findById(id));}
}
