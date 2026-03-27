package com.school.repository;
import com.school.entity.EmploiDuTemps;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface EmploiDuTempsRepository extends JpaRepository<EmploiDuTemps,Long> {
    List<EmploiDuTemps> findAllByOrderByDateCreationDesc();
}
