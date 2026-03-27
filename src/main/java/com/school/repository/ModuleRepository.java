package com.school.repository;
import com.school.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
public interface ModuleRepository extends JpaRepository<Module,Long> {
    List<Module> findByProfesseurId(Long id);
    @Query("SELECT m.nom FROM Module m WHERE m.professeur.id = :id")
    List<String> findNomsByProfesseurId(Long id);
}
