package com.school.repository;
import com.school.entity.Professeur;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface ProfesseurRepository extends JpaRepository<Professeur,Long> {
    Optional<Professeur> findByEmail(String email);
    Optional<Professeur> findByResponseToken(String token);
    boolean existsByEmail(String email);
}
