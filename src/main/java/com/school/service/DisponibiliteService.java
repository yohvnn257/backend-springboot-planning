package com.school.service;
import com.school.entity.Disponibilite;
import com.school.entity.Professeur;
import com.school.exception.ResourceNotFoundException;
import com.school.repository.DisponibiliteRepository;
import com.school.repository.ProfesseurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
@Service @RequiredArgsConstructor @Transactional(readOnly=true)
public class DisponibiliteService {
    private final DisponibiliteRepository repo;
    private final ProfesseurRepository profRepo;
    public List<Disponibilite> findAll(){return repo.findAll();}
    public List<Disponibilite> findBySemaine(LocalDate d,LocalDate f){return repo.findByJourBetween(d,f);}
    @Transactional public void delete(Long id){if(!repo.existsById(id))throw new ResourceNotFoundException("Disponibilité",id);repo.deleteById(id);}
}
