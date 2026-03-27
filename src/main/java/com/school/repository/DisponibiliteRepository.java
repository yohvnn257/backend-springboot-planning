package com.school.repository;
import com.school.entity.Disponibilite;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
public interface DisponibiliteRepository extends JpaRepository<Disponibilite,Long> {
    List<Disponibilite> findByProfesseurId(Long id);
    List<Disponibilite> findByJourBetween(LocalDate debut, LocalDate fin);
    List<Disponibilite> findByFiliereAndNiveauAndJourBetween(String f, String n, LocalDate d, LocalDate fin);
}
